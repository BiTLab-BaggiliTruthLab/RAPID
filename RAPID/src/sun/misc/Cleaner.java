/*
* Copyright (c) 2003, 2008, Oracle and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version 2 only, as
* published by the Free Software Foundation.  Oracle designates this
* particular file as subject to the "Classpath" exception as provided
* by Oracle in the LICENSE file that accompanied this code.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 2 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* You should have received a copy of the GNU General Public License version
* 2 along with this work; if not, write to the Free Software Foundation,
* Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
* or visit www.oracle.com if you need additional information or have any
* questions.
*/
package sun.misc;

import java.lang.ref.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Generate a Cleaner to proactively clean the buffer in memory
 * */
public class Cleaner extends PhantomReference
{

// Dummy reference queue, needed because the PhantomReference constructor
// insists that we pass a queue.  Nothing will ever be placed on this queue
// since the reference handler invokes cleaners explicitly.
//
private static final ReferenceQueue dummyQueue = new ReferenceQueue();

// Doubly-linked list of live cleaners, which prevents the cleaners
// themselves from being GC'd before their referents
//
static private Cleaner first = null;

private Cleaner
    next = null,
    prev = null;

private static synchronized Cleaner add(Cleaner cl) {
    if (first != null) {
        cl.next = first;
        first.prev = cl;
    }
    first = cl;
    return cl;
}

private static synchronized boolean remove(Cleaner cl) {

    // If already removed, do nothing
    if (cl.next == cl)
        return false;

    // Update list
    if (first == cl) {
        if (cl.next != null)
            first = cl.next;
        else
            first = cl.prev;
    }
    if (cl.next != null)
        cl.next.prev = cl.prev;
    if (cl.prev != null)
        cl.prev.next = cl.next;

    // Indicate removal by pointing the cleaner to itself
    cl.next = cl;
    cl.prev = cl;
    return true;

}

private final Runnable thunk;

Cleaner(Object referent, Runnable thunk) {
    super(referent, dummyQueue);
    this.thunk = thunk;
}

/**
 * Creates a new cleaner.
 *
 * @param  thunk
 *         The cleanup code to be run when the cleaner is invoked.  The
 *         cleanup code is run directly from the reference-handler thread,
 *         so it should be as simple and straightforward as possible.
 *
 * @return  The new cleaner
 */
public static Cleaner create(Object ob, Runnable thunk) {
    if (thunk == null)
        return null;
    return add(new Cleaner(ob, thunk));
}

/**
 * Runs this cleaner, if it has not been run before.
 */
public void clean() {
    if (!remove(this))
        return;
    try {
        thunk.run();
    } catch (final Throwable x) {
        AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    if (System.err != null)
                        new Error("Cleaner terminated abnormally", x)
                            .printStackTrace();
                    System.exit(1);
                    return null;
                }});
    }
}

}
