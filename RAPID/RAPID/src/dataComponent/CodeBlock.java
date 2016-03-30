package dataComponent;

import java.util.ArrayList;
/**
 * Code Block object
 * */
public class CodeBlock {
	/**the index of the method that a code block belongs to*/
	public int methodId;
	/**the offset for the start of a code block in DEX file*/
	public long startAddress;
	/**the offset for the end of a code block in DEX file*/
	public long endAddress;
	/**the index of a code block*/
	public int codeBlockId;
	/**The instruction list for a code block*/
	public ArrayList <Instruction> instructionList= new ArrayList <Instruction>();
	/**
	 * print out the instruction list.
	 * */
	public void printInsList(){
		System.out.println("Code block:	");
		for(Instruction ins: instructionList){
			ins.printFields();
		}
	}
}
