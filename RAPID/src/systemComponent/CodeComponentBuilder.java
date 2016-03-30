package systemComponent;

import java.nio.MappedByteBuffer;
import java.util.ArrayList;

import dataComponent.CodeBlock;
import dataComponent.CodeComponent;

import dataComponent.Instruction;
import dataComponent.MethodElement;


public class CodeComponentBuilder extends ComponentBuilder{
	/**creating a Code Component*/
	public CodeComponent codeComponent= new CodeComponent();
	/**creating a Method Component*/
	public MethodComponentBuilder mb = new MethodComponentBuilder(mbb);	
	
	private ArrayList<MethodElement> methodList=mb.ms.methodElementList;
	/**A construction method that calls the method in father class, which let Code Component find the DEX file in memory*/
	public CodeComponentBuilder(MappedByteBuffer mbb)  {
		super(mbb);
		// TODO Auto-generated constructor stub
		
		buildComponent();
	}
	private void buildComponent() {
		
		getSectionOffsets();
		loadCodeBlockToMethod();
		
	}
	/**
	 * parsing the code block into instructions
	 * */
	public void loadInstructionToCodeBlock(){
		parseCodeToStatement();
	}

	private void parseCodeToStatement(){
		for(int i=0;i<codeComponent.codeBlockList.size();i++){//cs.codeStream.size()
			
			long startAddress=codeComponent.codeBlockList.get(i).startAddress+16;
			long endAddress=codeComponent.codeBlockList.get(i).endAddress;
			int serialNum=0;
			 while(startAddress<endAddress){
				 	
				 	//System.out.println("startAddress:"+startAddress);
				 	readBinary(startAddress,1,"ADDRESS");
				 	int op=(int)tempValue;
				 	Instruction ins= new Instruction(this);
				 	ins.codeBlockId=i;
				 	ins.instructionId=serialNum;
				 	if(op!=0){
				 			ins.address=startAddress;
				 			ins.identifyToken(op);
				 			
				 			
				 	}else if(op==0){

						 	readBinary(startAddress,2,"ADDRESS");
						 	op=(int)tempValue;	
						 	
						 	
						 	if(op!=0){
						 		
						 		readBinary(startAddress+2,2,"ADDRESS");
						 		int size=(int)tempValue;
						 		switch(op){
								case 0x100:								 											
									ins.op=op;
									ins.opcode="packed-switch-payload";
									ins.length = (size*2+4)*2;	
									ins.address=startAddress;								
									break;
								case 0x200:	
									ins.op=op;
									ins.opcode="sparse-switch-payload";
									ins.length = (size*4+2)*2;
									ins.address=startAddress;
									break;
								case 0x300:	
									readBinary(startAddress+2+2,4,"ADDRESS");
									int size2=(int)tempValue;
									ins.op=op;
									ins.opcode="array-data-payload";
									ins.length=((size*size2+1)/2+4)*2;
									ins.address=startAddress;
									break;
								default:
							 		ins.length=1;
							 		ins.op=op;
							 		ins.opcode="nod";
							 		ins.address=startAddress;
									break;
								}

						 	}else if (op==0x0){
						 		ins.length=1;
						 		ins.op=op;
						 		ins.opcode="nod";
						 		ins.address=startAddress;
						 	}
						 	
	 		
				 	}
				 	
				 	codeComponent.codeBlockList.get(i).instructionList.add(ins);
				 				 	
				 	startAddress=startAddress+ins.length;
				 	serialNum++;
			 }

		}
		
	}
	private void loadCodeBlockToMethod() {		
		long startAddress=this.ClassDataStart;	
		long sectionSize=this.ClassDataSize;	
		long[] methodCount = new long[4];

		for(int j=0;j<sectionSize;j++){//sectionSize
				for(int i=0;i<4;i++){//class header 
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
						//tempUlebValue
						//tempUlebByteNum
					
					methodCount[i]=tempUlebValue;
					startAddress=startAddress+tempUlebByteNum;
				}
				
				for(int i=0;i<methodCount[0];i++){
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field1
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field2
				}//Static field count
				for(int i=0;i<methodCount[1];i++){
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field1
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field2
				}//Instance field count
				int methodId=0;
				for(int i=0;i<methodCount[2];i++){
					CodeBlock ce = new CodeBlock();
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					methodId=methodId+(int) tempUlebValue;					
					ce.methodId=methodId;
					//field1 Method id
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field2 Access flags
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field3 Method offset
					ce.startAddress=tempUlebValue;
					if(tempUlebValue!=0){
						readBinary(tempUlebValue+2+2+2+2+4,4,"ADDRESS");//# Number of registers
																		//# Size of input args
																		//# Size of input args
																		//# Number of try_items
																		//# Debug info
						long bytecodeSize=tempValue;
						ce.endAddress=ce.startAddress+2+2+2+2+4+4+(bytecodeSize*2);
						MethodElement method=methodList.get(methodId);
						method.hasCodeBlock=true;
						method.codeBlock=ce;
					}else{
						ce.endAddress=0;
					}
					codeComponent.codeBlockList.add(ce);
					
					//System.out.println(msb.ms.methodStream.get(methodId).codeStartAddress);
				}//Direct method count
				methodId=0;
				
				for(int i=0;i<methodCount[3];i++){
					CodeBlock ce = new CodeBlock();
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field1 Method id
					methodId=methodId+(int) tempUlebValue;
					ce.methodId=methodId;
					
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					//field2 Access flags
					startAddress=startAddress+tempUlebByteNum;
					readBinary(startAddress,1,"ADDRESS");
					getUleb(startAddress,tempValue);
					startAddress=startAddress+tempUlebByteNum;
					//field3 Method offset
					ce.startAddress=tempUlebValue;
					if(tempUlebValue!=0){
						readBinary(tempUlebValue+2+2+2+2+4,4,"ADDRESS");//# Number of registers
																		//# Size of input args
																		//# Size of input args
																		//# Number of try_items
																		//# Debug info
						long bytecodeSize=tempValue;
						ce.endAddress=ce.startAddress+2+2+2+2+4+4+(bytecodeSize*2);
						
						MethodElement method=methodList.get(methodId);
						method.hasCodeBlock=true;
						method.codeBlock=ce;
					}else{
						ce.endAddress=0;
					}
					codeComponent.codeBlockList.add(ce);
				}//Virtual method count
				
		}
				

	}

}
