package systemComponent;

import java.io.File;
import java.io.FileInputStream;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
/**loading unzipped DEX file in memory*/
public class DexLoader {
	private FileChannel fin = null;
	private FileInputStream input = null;	
	/**the instance of the buffer in memory where stores the DEX file*/
	public MappedByteBuffer mbb;
	
	private String unzippedFileDir;
	
	public DexLoader(String unzippedFileDir){
		this.unzippedFileDir=unzippedFileDir;
		getCodeBuffer();
		
	}
	
	private void getCodeBuffer()  {
		
		File file = new File(this.unzippedFileDir);		
	try{
			input = new FileInputStream(file);
			fin= input.getChannel();		
			mbb = fin.map(FileChannel.MapMode.READ_ONLY,0,file.length());
		}catch(Exception e){
			System.out.println("ERROR:	Sorry, Directory: "+unzippedFileDir+" does not exist.");
		}
				

	}
	
	
	/**cleaning the buffer*/
	public void cleanBuffer(){
		
		AccessController.doPrivileged(new PrivilegedAction<Object>() {   
		  public Object run() {   
		    try {   
		       Method getCleanerMethod = mbb.getClass().getMethod("cleaner", new Class[0]);   
		       getCleanerMethod.setAccessible(true);   
		       sun.misc.Cleaner cleaner = (sun.misc.Cleaner)   
		       getCleanerMethod.invoke(mbb, new Object[0]);   
		       cleaner.clean();
		       fin.close();
		       input.close();   
		     } catch (Exception e) {  
		    	 System.out.println("ERROR:	Sorry cleanning buffer has occured exceptions.");
		    	 e.printStackTrace();   
		     }   
		    return null;   
		   }   
		});		
	}	
}
