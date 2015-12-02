package dataComponent;



import java.io.FileNotFoundException;

import systemComponent.CodeComponentBuilder;

/**
 * Instruction object
 * */
public class Instruction {
	/**the offset of an instruction in DEX file*/
	public long address;
	/**the index number of an instruction in the instruction list of a code block */
	public int instructionId;
	/**the hex format of the opcode*/
	public int op;
	/**the string format of a opcode*/
	public String opcode;
	/**the length of an instruction in DEX file*/
	public int length;
	/**
	 * The Code block Id this instruction belongs to
	 **/
	public int codeBlockId;
	private boolean hasRegister=false;
	/**
	 * True if the instruction has an operand
	 * */
	public boolean hasOperand=false;
	/**
	 * which named as same as in google's DEX format web site,
	 * which is also the explanation for the opcode
	 * */
	public String opcodeSuffix=null;
	/**
	 * the meaning of the operand of an instruction
	 * */
	public String operandSuffix=null;
	
	protected int registerNum;
	protected int regStartByte;
	protected int regLength;
	protected int oprStartByte;
	protected int oprLength;
	
	/**the content of the operand*/
	private long operand;
	private int[] registerList;// 
	private CodeComponentBuilder ccb;
	public Instruction(){
		
	}
	/**A construction method that calls the method in father class 'MethodComponent', 
	 * which let an instance of Instruction class finding the DEX file in memory
	 * @param ccb DEX file loaded in memory
	 * */
	public Instruction(CodeComponentBuilder ccb){
		this.ccb=ccb;
	}
	/**print fields of an instruction*/
	public void printFields(){
		if(opcodeSuffix!=null){
			System.out.print("	"+this.address+"	"+this.opcodeSuffix+"	");
		}else{
			System.out.print("	"+this.address+"	"+this.opcode+"	");
		}
		if (this.hasRegister==true){
			for(int i=0;i<this.registerList.length;i++){
				if (i!=0){
					System.out.print(",");
				}
				System.out.print("v"+this.registerList[i]);
				
				
			}
		}
		if (this.hasOperand==true){
			System.out.print("	"+this.operand+"	#"+this.operandSuffix);
			instantiateOperand();
		}
		System.out.println("");
	}
	/**
	 * @param staticValueType is the type of the static value for your search, 
	 * which currently support "STRING".
	 * 
	 * @param registerNumber the number (start with 0) of the register you want to search, which may out of the bounds
	 * */
	public String staticBackTrace(String staticValueType,int registerNumber){
		int regNmu=-1;				
		boolean found=false;
		String staticValue="";
		if(this.registerList.length>registerNumber){
			regNmu=this.registerList[registerNumber];
		}else{
			try{
				throw new FileNotFoundException("WARNING:	the registerNum indicated for back tracing is out of Bounds. "
					+ "\n	ERROR occured by instruction address: "+this.address);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	

				for(int i=this.instructionId-1;i>=0;i--){
					
					Instruction ins=ccb.codeComponent.codeBlockList.get(this.codeBlockId).instructionList.get(i);
					
					
					//if ... (control flow opcode)then jump to .. .
					if (this.hasRegister()&&regNmu!=-1&&ins.registerList!=null){
						for(int reg:ins.registerList){
							
							if (regNmu==reg){
								found=true;
							}
							
						}
						if(found==true){
							
								if(staticValueType.equals("STRING")){//searching string type static 
									
									if (ins.opcode.startsWith("const-string")){
										
										staticValue=this.ccb.mb.ssb.ss.stringElementList.get((int)ins.operand).stringContent;	
										return staticValue;
										
									}
									
								}																							
								break;
						}
					}						
				}
		

		return staticValue;

	}
	/**get the list filled with registers that is order by destination register first*/
	protected void parseOperand(){
		
			ccb.readBinary(this.address+oprStartByte, oprLength, "ADDRESS");
			operand=(long)ccb.tempValue;
		
	}

	private void vAvB(){
		this.hasRegister=true;
		this.registerList=new int[2];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int) (ccb.tempValue-ccb.tempValue/16*16);
		registerList[1]=(int)ccb.tempValue/16;
	}
	private void vAA(){
		this.hasRegister=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
	}
	private void vAAvBBvCC(){
		this.hasRegister=true;
		this.registerList=new int[3];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 1, "ADDRESS");
		registerList[1]=(int)ccb.tempValue;
		ccb.readBinary(this.address+3, 1, "ADDRESS");
		registerList[2]=(int)ccb.tempValue;
	}
	private void vAAvBBCC(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[2];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 1, "ADDRESS");
		registerList[1]=(int)ccb.tempValue;
		ccb.readBinary(this.address+3, 1, "ADDRESS");
		this.operand=ccb.tempValue;
	}
	private void vAAvBBBB(){
		this.hasRegister=true;
		this.registerList=new int[2];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		registerList[1]=(int)ccb.tempValue;
	}
	private void vAAAAvBBBB(){
		this.hasRegister=true;
		this.registerList=new int[2];
		ccb.readBinary(this.address+1, 2, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+3, 2, "ADDRESS");
		registerList[1]=(int)ccb.tempValue;
	}
	private void vAB(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		this.operand=(int)ccb.tempValue/16;
		registerList[0]=(int) (ccb.tempValue-ccb.tempValue/16*16);
	}
	private void vAABBBB(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		this.operand=(int)ccb.tempValue;
	}
	private void vAA8B(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		int low=(int)ccb.tempValue;
		ccb.readBinary(this.address+4, 2, "ADDRESS");
		int high=(int) ccb.tempValue;
		
		this.operand=(high<<16)+low;		
	}
	private void vAABBBB0000(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		this.operand=ccb.tempValue<<16;
	}
	private void vAA16B(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		int low8=(int) ccb.tempValue;
		ccb.readBinary(this.address+4, 2, "ADDRESS");
		long low16=ccb.tempValue<<16+low8;
		ccb.readBinary(this.address+6, 2, "ADDRESS");
		int high8=(int) ccb.tempValue;
		ccb.readBinary(this.address+8, 2, "ADDRESS");
		long high16=ccb.tempValue<<16+high8;
		this.operand=high16<<32+low16;
	}
	private void vAABBBB12(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[1];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int)ccb.tempValue;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		this.operand=ccb.tempValue<<48;
	}
	private void vAvBCCCC(){
		this.hasRegister=true;
		this.hasOperand=true;
		this.registerList=new int[2];
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		registerList[0]=(int) (ccb.tempValue-ccb.tempValue/16*16);
		registerList[1]=(int)ccb.tempValue/16;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		this.operand=(int)ccb.tempValue;
	}
	private void AA(){
		this.hasOperand=true;
		ccb.readBinary(this.address+1, 1, "ADDRESS");
		this.operand=ccb.tempValue;
	}
	private void AAAA(){
		this.hasOperand=true;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		this.operand=ccb.tempValue;
	}
	private void AAAAAAAA(){
		this.hasOperand=true;
		ccb.readBinary(this.address+2, 2, "ADDRESS");
		int low=(int)ccb.tempValue;
		ccb.readBinary(this.address+4, 2, "ADDRESS");
		int high=(int) ccb.tempValue;
		this.operand=high<<16+low;	
	}
	private void vCvDvEvFvGBBBB(){

		
		this.regStartByte=1;
		this.regLength=1;
		ccb.readBinary(this.address+this.regStartByte, this.regLength, "ADDRESS");
		if(ccb.tempValue!=0){
			this.registerNum =(int) (ccb.tempValue/16);
			int fifthReg=(int)(ccb.tempValue-ccb.tempValue/16*16);
			this.hasRegister=true;
			this.regStartByte=4;
			this.registerList =new int[this.registerNum];
			ccb.readBinary(this.address+this.regStartByte,regLength,"ADDRESS");
			long low=(int)ccb.tempValue;
			ccb.readBinary(this.address+this.regStartByte+1,regLength,"ADDRESS");
			long high= ccb.tempValue;
			switch (this.registerNum){
			case 5:
					this.registerList[4] = fifthReg;
			case 4: 
					this.registerList[3] = (int) (high/16);
			case 3: 
					this.registerList[2] = (int) (high-high/16*16);
			case 2: 
					this.registerList[1] = (int) (low/16);
			case 1: 
					this.registerList[0] = (int) (low-low/16*16);
					
			default:break;
			}
													
	
		}else {
			this.registerNum=0;
			
		}

		this.hasOperand=true;
		this.oprStartByte=2;
		this.oprLength=2;
		this.parseOperand();
	}
	private void vCCCCvNNNNBBBB(){
		this.regStartByte=1;
		this.regLength=1;
		ccb.readBinary(this.address+this.regStartByte, this.regLength, "ADDRESS");
		if(ccb.tempValue!=0){
			this.hasRegister=true;
			this.registerNum =(int) (ccb.tempValue);
			this.registerList =new int[this.registerNum];
			ccb.readBinary(this.address+4, 2, "ADDRESS");
			int startNum=(int) (ccb.tempValue);
			for(int i=0;i<registerNum;i++){
				this.registerList[i]=startNum;
				startNum++;
			}
		}else ccb.tempValue=0;
		
		this.hasOperand=true;
		this.oprStartByte=2;
		this.oprLength=2;
		this.parseOperand();
	}
	//
	public int identifyToken(int op){
		switch(op){
			case 0x1:
				this.length= 2;
				this.opcode="move";
				vAvB();				
				return this.length;
			case 0x2:
				this.length= 4;
				this.opcode="move/from16";
				vAAvBBBB();
				return this.length;
			case 0x3:
				this.length= 5;
				this.opcode="move/16";
				vAAAAvBBBB();
				return this.length;
			case 0x4:
				this.length= 2;
				this.opcode="move-wide";
				vAvB();	
				
				return this.length;
			case 0x5:
				this.length= 4;
				this.opcode="move-wide/from16";
				vAAvBBBB();	
				return this.length;
			case 0x6:
				this.length= 5;
				this.opcode="move-wide/16";				
				vAAAAvBBBB();	
				return this.length;
			case 0x7:
				this.length= 2;
				this.opcode="move-object";
				vAvB();
				return this.length;
			case 0x8:
				this.length= 4;
				this.opcode="move-object/from16";
				vAAvBBBB();	
				return this.length;
			case 0x9:
				this.length= 5;
				this.opcode="move-object/16";
				vAAAAvBBBB();	
				return this.length;
			case 0xa:
				this.length= 2;
				this.opcode="move-result";
				vAA();
				return this.length;
			case 0xb:
				this.length= 2;
				this.opcode="move-result-wide";
				vAA();
				return this.length;
			case 0xc:
				this.length= 2;
				this.opcode="move-result-object";
				vAA();
				return this.length;
			case 0xd:
				this.length= 2;
				this.opcode="move-exception";
				vAA();
				return this.length;
			case 0xe:
				this.length= 2;
				this.opcode="return-void";
				return this.length;
			case 0xf:
				this.length= 2;
				this.opcode="return";
				vAA();
				return this.length;
			case 0x10:
				this.length= 2;
				this.opcode="return-wide";
				vAA();
				return this.length;
			case 0x11:
				this.length= 2;
				this.opcode="return-object";
				vAA();
				return this.length;
			case 0x12:
				this.length= 2;
				this.opcode="const/4";
				vAB();
				this.operandSuffix="int(4 bits)";
				return this.length;
			case 0x13:
				this.length= 4;
				this.opcode="const/16";
				vAABBBB();
				this.operandSuffix="int(16 bits)";
				return this.length;
			case 0x14:
				this.length= 6;
				this.opcode="const";
				vAA8B();
				this.operandSuffix="constant(32-bit)";
				return this.length;
			case 0x15:
				this.length= 4;
				this.opcode="const/high16";
				vAABBBB0000();
				this.operandSuffix="int(32 bits)";
				return this.length;
			case 0x16:
				this.length= 4;
				this.opcode="const-wide/16";
				vAABBBB();
				this.operandSuffix="int(16 bits)";
				return this.length;
			case 0x17:
				this.length= 6;
				this.opcode="const-wide/32";
				vAA8B();
				this.operandSuffix="int(32 bits)";
				return this.length;
			case 0x18:
				this.length= 10;
				this.opcode="const-wide";
				vAA16B();
				this.operandSuffix="int(64 bits)";
				return this.length;
			case 0x19:
				this.length= 4;
				this.opcode="const-wide/high16";				
				vAABBBB12();
				this.operandSuffix="int(64 bits)";
				return this.length;
			case 0x1a:
				this.length= 4;
				this.opcode="const-string";
				vAABBBB();
				this.operandSuffix="string id";
				return this.length;
			case 0x1b:
				this.length= 6;
				this.opcode="const-string/jumbo";
				vAA8B();
				this.operandSuffix="string id";
				return this.length;
			case 0x1c:
				this.length= 4;
				this.opcode="const-class";
				vAABBBB();
				this.operandSuffix="type id";
				return this.length;
			case 0x1d:
				this.length= 2;
				this.opcode="monitor-enter";
				vAA();
				return this.length;
			case 0x1e:
				this.length= 2;
				this.opcode="monitor-exit";
				vAA();
				return this.length;
			case 0x1f:
				this.length= 4;
				this.opcode="check-cast";
				vAABBBB();
				this.operandSuffix="type id";
				return this.length;
			case 0x20:
				this.length= 4;
				this.opcode="instance-of";
				vAvBCCCC();
				this.operandSuffix="type id";
				return this.length;
			case 0x21:
				this.length= 2;
				this.opcode="array-length";
				vAvB();
				return this.length;
			case 0x22:
				this.length= 4;
				this.opcode="new-instance";
				vAABBBB();
				this.operandSuffix="type id";
				return this.length;
			case 0x23:
				this.length= 4;
				this.opcode="new-array";
				vAvBCCCC();
				this.operandSuffix="type id";
				return this.length;
			case 0x24:
				this.length= 6;
				this.opcode="filled-new-array";
				vCvDvEvFvGBBBB();
				this.operandSuffix="type id";
				return this.length;
			case 0x25:
				this.length= 6;
				this.opcode="filled-new-array/range";
				vCCCCvNNNNBBBB();
				this.operandSuffix="type id";
				return this.length;
			case 0x26:
				this.length= 6;
				this.opcode="fill-array-data";				
				vAA8B();
				this.operandSuffix="array-data-payload offset";
				 if (this.operand>=32768){
					 this.operand-=65536;
				 }
				 this.operand=this.address+(this.operand*2);
				return this.length;
			case 0x27:
				this.length= 2;
				this.opcode="throw";
				vAA();
				return this.length;
			case 0x28:
				this.length= 2;
				this.opcode="goto";
				AA();
				this.operandSuffix="goto offset";
				 if (this.operand>=8){
					 this.operand-=16;
				 }
				 this.operand=this.address+(this.operand*2);
				return this.length;
			case 0x29:
				this.length= 4;
				this.opcode="goto/16";
				AAAA();
				this.operandSuffix="goto offset";
				 if (this.operand>=128){
					 this.operand-=256;
				 }
				 this.operand=this.address+(this.operand*2);
				return this.length;
			case 0x2a:
				this.length= 6;
				this.opcode="goto/32";
				AAAAAAAA();
				this.operandSuffix="goto offset";
				 if (this.operand>=32768){
					 this.operand-=65536;
				 }
				 this.operand=this.address+(this.operand*2);
				return this.length;
			case 0x2b:
				this.length= 6;
				this.opcode="packed-switch";
				vAA8B();
				this.operandSuffix="packed-switch-payload offset";
				if (this.operand>=32768){
					 this.operand-=65536;
				 }
				this.operand=this.address+(this.operand*2);
				return this.length;
			case 0x2c:
				this.length= 6;
				this.opcode="sparse-switch";
				vAA8B();
				this.operandSuffix="sparse-switch-payload offset";
				if (this.operand>=32768){
					 this.operand-=65536;
				 }
				this.operand=this.address+(this.operand*2);
				return this.length;
			default:
				if					((0x3e<=op && op<=0x43) | op==0x73 | (0x79<=op && op<=0x7a) | op>=0xe3){
					this.length= 2;
					this.opcode="UNUSED";
					return this.length;
				}else				if(0x2d<=op && op<=0x31){
					this.length= 4;
					this.opcode="cmpkind";
					switch (op){
						case 0x2d:this.opcodeSuffix="cmpl-float";break;
						case 0x2e:this.opcodeSuffix="cmpg-float";break;
						case 0x2f:this.opcodeSuffix="cmpl-double";break;
						case 0x30:this.opcodeSuffix="cmpg-double";break;
						case 0x31:this.opcodeSuffix="cmp-long";break;
					}
					vAAvBBvCC();
					return this.length;
				}else 				if(0x32<=op && op<=0x37){
					this.length= 4;
					this.opcode="if-test";
					switch (op){
						case 0x32: this.opcodeSuffix="if-eq";break;
						case 0x33: this.opcodeSuffix="if-ne";break;
						case 0x34: this.opcodeSuffix="if-lt";break;
						case 0x35: this.opcodeSuffix="if-ge";break;
						case 0x36: this.opcodeSuffix="if-gt";break;
						case 0x37: this.opcodeSuffix="if-le";break;
					}
					vAvBCCCC();
					this.operandSuffix="goto offset";
					 if (this.operand>=128){
						 this.operand-=256;						 
					 }
					 this.operand=this.address+(this.operand*2);
					return this.length;
				}else				if(0x38<=op && op<=0x3d){
					this.length= 4;
					this.opcode="if-testz";
					switch (op){
					case 0x38: this.opcodeSuffix="if-eqz";break;
					case 0x39: this.opcodeSuffix="if-nez";break;
					case 0x3a: this.opcodeSuffix="if-ltz";break;
					case 0x3b: this.opcodeSuffix="if-gez";break;
					case 0x3c: this.opcodeSuffix="if-gtz";break;
					case 0x3d: this.opcodeSuffix="if-lez";break;
				}
					vAABBBB();
					this.operandSuffix="goto offset";
					 if (this.operand>=128){
						 this.operand-=256;						 
					 }
					 this.operand=this.address+(this.operand*2);
					return this.length;
				}else				if(0x44<=op && op<=0x51){
					this.length= 4;
					this.opcode="arrayop";
					switch(op){
						case 0x44: this.opcodeSuffix="aget";break;
						case 0x45: this.opcodeSuffix="aget-wide";break;
						case 0x46: this.opcodeSuffix="aget-object";break;
						case 0x47: this.opcodeSuffix="aget-boolean";break;
						case 0x48: this.opcodeSuffix="aget-byte";break;
						case 0x49: this.opcodeSuffix="aget-char";break;
						case 0x4a: this.opcodeSuffix="aget-short";break;
						case 0x4b: this.opcodeSuffix="aput";break;
						case 0x4c: this.opcodeSuffix="aput-wide";break;
						case 0x4d: this.opcodeSuffix="aput-object";break;
						case 0x4e: this.opcodeSuffix="aput-boolean";break;
						case 0x4f: this.opcodeSuffix="aput-byte";break;
						case 0x50: this.opcodeSuffix="aput-char";break;
						case 0x51: this.opcodeSuffix="aput-short";break;
					}
					vAAvBBvCC();
					return this.length;
				}else				if(0x52<=op && op<=0x5f){
					this.length= 4;
					this.opcode="iinstanceop";
					switch(op){
					case 0x52: this.opcodeSuffix="iget";break;
					case 0x53: this.opcodeSuffix="iget-wide";break;
					case 0x54: this.opcodeSuffix="iget-object";break;
					case 0x55: this.opcodeSuffix="iget-boolean";break;
					case 0x56: this.opcodeSuffix="iget-byte";break;
					case 0x57: this.opcodeSuffix="iget-char";break;
					case 0x58: this.opcodeSuffix="iget-short";break;
					case 0x59: this.opcodeSuffix="iput";break;
					case 0x5a: this.opcodeSuffix="iput-wide";break;
					case 0x5b: this.opcodeSuffix="iput-object";break;
					case 0x5c: this.opcodeSuffix="iput-boolean";break;
					case 0x5d: this.opcodeSuffix="iput-byte";break;
					case 0x5e: this.opcodeSuffix="iput-char";break;
					case 0x5f: this.opcodeSuffix="iput-short";break;
					}
					vAvBCCCC();
					this.operandSuffix="field id";
					return this.length;
				}else				if(0x60<=op && op<=0x6d){
					this.length= 4;
					this.opcode="sstaticop";
					switch(op){
						case 0x60: this.opcodeSuffix="sget";break;
						case 0x61: this.opcodeSuffix="sget-wide";break;
						case 0x62: this.opcodeSuffix="sget-object";break;
						case 0x63: this.opcodeSuffix="sget-boolean";break;
						case 0x64: this.opcodeSuffix="sget-byte";break;
						case 0x65: this.opcodeSuffix="sget-char";break;
						case 0x66: this.opcodeSuffix="sget-short";break;
						case 0x67: this.opcodeSuffix="sput";break;
						case 0x68: this.opcodeSuffix="sput-wide";break;
						case 0x69: this.opcodeSuffix="sput-object";break;
						case 0x6a: this.opcodeSuffix="sput-boolean";break;
						case 0x6b: this.opcodeSuffix="sput-byte";break;
						case 0x6c: this.opcodeSuffix="sput-char";break;
						case 0x6d: this.opcodeSuffix="sput-short";break;
					}
					vAABBBB();
					this.operandSuffix= "field id";
					return this.length;
				}else				if(0x6e<=op && op<=0x72){//
					this.length= 6;
					this.opcode="invoke-kind";
					switch(op){
						case 0x6e: this.opcodeSuffix="invoke-virtual";break;
						case 0x6f: this.opcodeSuffix="invoke-super";break;
						case 0x70: this.opcodeSuffix="invoke-direct";break;
						case 0x71: this.opcodeSuffix="invoke-static";break;
						case 0x72: this.opcodeSuffix="invoke-interface";break;
					}
					vCvDvEvFvGBBBB();
									this.operandSuffix="method id";
					return this.length;
				}else				if(0x74<=op && op<=0x78){//
					this.length= 6;
					this.opcode="invoke-kind/range";
					switch(op){
						case 0x74: this.opcodeSuffix="invoke-virtual/range";break;
						case 0x75: this.opcodeSuffix="invoke-super/range";break;
						case 0x76: this.opcodeSuffix="invoke-direct/range";break;
						case 0x77: this.opcodeSuffix="invoke-static/range";break;
						case 0x78: this.opcodeSuffix="invoke-interface/range";break;
					
					}
					vCCCCvNNNNBBBB();
					this.operandSuffix="method id";
					return this.length;
				}else				if(0x7b<=op && op<=0x8f){
					this.length= 2;
					this.opcode="unop";
					switch(op){
						case 0x7b: this.opcodeSuffix="neg-int";break;
						case 0x7c: this.opcodeSuffix="not-int";break;
						case 0x7d: this.opcodeSuffix="neg-long";break;
						case 0x7e: this.opcodeSuffix="not-long";break;
						case 0x7f: this.opcodeSuffix="neg-float";break;
						case 0x80: this.opcodeSuffix="neg-double";break;
						case 0x81: this.opcodeSuffix="int-to-long";break;
						case 0x82: this.opcodeSuffix="int-to-float";break;
						case 0x83: this.opcodeSuffix="int-to-double";break;
						case 0x84: this.opcodeSuffix="long-to-int";break;
						case 0x85: this.opcodeSuffix="long-to-float";break;
						case 0x86: this.opcodeSuffix="long-to-double";break;
						case 0x87: this.opcodeSuffix="float-to-int";break;
						case 0x88: this.opcodeSuffix="float-to-long";break;
						case 0x89: this.opcodeSuffix="float-to-double";break;
						case 0x8a: this.opcodeSuffix="double-to-int";break;
						case 0x8b: this.opcodeSuffix="double-to-long";break;
						case 0x8c: this.opcodeSuffix="double-to-float";break;
						case 0x8d: this.opcodeSuffix="int-to-byte";break;
						case 0x8e: this.opcodeSuffix="int-to-char";break;
						case 0x8f: this.opcodeSuffix="int-to-short";break;
					}
					
					vAvB();
					return this.length;
				}else				if(0x90<=op && op<=0xaf){
					this.length= 4;
					this.opcode="binop";
					switch(op){
						case 0x90: this.opcodeSuffix="add-int";break;
						case 0x91: this.opcodeSuffix="sub-int";break;
						case 0x92: this.opcodeSuffix=" mul-int";break;
						case 0x93: this.opcodeSuffix="div-int";break;
						case 0x94: this.opcodeSuffix="rem-int";break;
						case 0x95: this.opcodeSuffix="and-int";break;
						case 0x96: this.opcodeSuffix="or-int";break;
						case 0x97: this.opcodeSuffix="xor-int";break;
						case 0x98: this.opcodeSuffix="shl-int";break;
						case 0x99: this.opcodeSuffix="shr-int";break;
						case 0x9a: this.opcodeSuffix="ushr-int";break;
						case 0x9b: this.opcodeSuffix="add-long";break;
						case 0x9c: this.opcodeSuffix="sub-long";break;
						case 0x9d: this.opcodeSuffix="mul-long";break;
						case 0x9e: this.opcodeSuffix="div-long";break;
						case 0x9f: this.opcodeSuffix="rem-long";break;
						case 0xa0: this.opcodeSuffix="and-long";break;
						case 0xa1: this.opcodeSuffix="or-long";break;
						case 0xa2: this.opcodeSuffix="xor-long";break;
						case 0xa3: this.opcodeSuffix="shl-long";break;
						case 0xa4: this.opcodeSuffix="shr-long";break;
						case 0xa5: this.opcodeSuffix="ushr-long";break;
						case 0xa6: this.opcodeSuffix="add-float";break;
						case 0xa7: this.opcodeSuffix="sub-float";break;
						case 0xa8: this.opcodeSuffix="mul-float";break;
						case 0xa9: this.opcodeSuffix="div-float";break;
						case 0xaa: this.opcodeSuffix="rem-float";break;
						case 0xab: this.opcodeSuffix="add-double";break;
						case 0xac: this.opcodeSuffix="sub-double";break;
						case 0xad: this.opcodeSuffix="mul-double";break;
						case 0xae: this.opcodeSuffix="div-double";break;
						case 0xaf: this.opcodeSuffix="rem-double";break;
					}
					vAAvBBvCC();
					return this.length;
				}else				if(0xb0<=op && op<=0xcf){
					this.length= 2;
					this.opcode="binop/2addr";
					switch(op){
						case 0xb0: this.opcodeSuffix="add-int/2addr";break;
						case 0xb1: this.opcodeSuffix="sub-int/2addr";break;
						case 0xb2: this.opcodeSuffix="mul-int/2addr";break;
						case 0xb3: this.opcodeSuffix="div-int/2addr";break;
						case 0xb4: this.opcodeSuffix="rem-int/2addr";break;
						case 0xb5: this.opcodeSuffix="and-int/2addr";break;
						case 0xb6: this.opcodeSuffix="or-int/2addr";break;
						case 0xb7: this.opcodeSuffix="xor-int/2addr";break;
						case 0xb8: this.opcodeSuffix="shl-int/2addr";break;
						case 0xb9: this.opcodeSuffix="shr-int/2addr";break;
						case 0xba: this.opcodeSuffix="ushr-int/2addr";break;
						case 0xbb: this.opcodeSuffix="add-long/2addr";break;
						case 0xbc: this.opcodeSuffix="sub-long/2addr";break;
						case 0xbd: this.opcodeSuffix="mul-long/2addr";break;
						case 0xbe: this.opcodeSuffix="div-long/2addr";break;
						case 0xbf: this.opcodeSuffix="rem-long/2addr";break;
						case 0xc0: this.opcodeSuffix="and-long/2addr";break;
						case 0xc1: this.opcodeSuffix="or-long/2addr";break;
						case 0xc2: this.opcodeSuffix="xor-long/2addr";break;
						case 0xc3: this.opcodeSuffix="shl-long/2addr";break;
						case 0xc4: this.opcodeSuffix="shr-long/2addr";break;
						case 0xc5: this.opcodeSuffix="ushr-long/2addr";break;
						case 0xc6: this.opcodeSuffix="add-float/2addr";break;
						case 0xc7: this.opcodeSuffix="sub-float/2addr";break;
						case 0xc8: this.opcodeSuffix="mul-float/2addr";break;
						case 0xc9: this.opcodeSuffix="div-float/2addr";break;
						case 0xca: this.opcodeSuffix="rem-float/2addr";break;
						case 0xcb: this.opcodeSuffix="add-double/2addr";break;
						case 0xcc: this.opcodeSuffix="sub-double/2addr";break;
						case 0xcd: this.opcodeSuffix="mul-double/2addr";break;
						case 0xce: this.opcodeSuffix="div-double/2addr";break;
						case 0xcf: this.opcodeSuffix="rem-double/2addr";break;
					}
					vAvB();
					return this.length;
				}else 				if(0xd0<=op && op<=0xd7){
					this.length= 4;
					this.opcode="binop/lit16";
					switch(op){
						case 0xd0: this.opcodeSuffix="add-int/lit16";break;
						case 0xd1: this.opcodeSuffix="rsub-int";break;
						case 0xd2: this.opcodeSuffix="mul-int/lit16";break;
						case 0xd3: this.opcodeSuffix="div-int/lit16";break;
						case 0xd4: this.opcodeSuffix="rem-int/lit16";break;
						case 0xd5: this.opcodeSuffix="and-int/lit16";break;
						case 0xd6: this.opcodeSuffix="or-int/lit16";break;
						case 0xd7: this.opcodeSuffix="xor-int/lit16";break;
						
					}
					vAvBCCCC();
					this.operandSuffix="constant(16 bits)";
					return this.length;
				}else 				if(op>=0xd8 && op<=0xe2){
					this.length= 4;
					this.opcode="binop/lit8";
					switch(op){
					
						case 0xd8: this.opcodeSuffix="add-int/lit8";break;
						case 0xd9: this.opcodeSuffix="rsub-int/lit8";break;
						case 0xda: this.opcodeSuffix="mul-int/lit8";break;
						case 0xdb: this.opcodeSuffix="div-int/lit8";break;
						case 0xdc: this.opcodeSuffix="rem-int/lit8";break;
						case 0xdd: this.opcodeSuffix="and-int/lit8";break;
						case 0xde: this.opcodeSuffix="or-int/lit8";break;
						case 0xdf: this.opcodeSuffix="xor-int/lit8";break;
						case 0xe0: this.opcodeSuffix="shl-int/lit8";break;
						case 0xe1: this.opcodeSuffix="shr-int/lit8";break;
						case 0xe2: this.opcodeSuffix="ushr-int/lit8";break;
					}
					vAAvBBCC();
					this.operandSuffix="constant(8 bits)";
					return this.length;
				}
		}
		return -1;
	}
	/**
	 * Return true if this instruction has a register list
	 * */
	public boolean hasRegister(){
		if(this.registerList!=null){
			return true;
		}else
			return false;
	}
	/**
	 * Obtain the register list of a instruction.
	 * */
	public int[] getRegList(){
			return this.registerList;
	}
	/**
	 * Obtain the operand of a instruction
	 * */
	public long getOperand(){
		return this.operand;
	}
	public void instantiateOperand() {
		switch(this.operandSuffix){
		case "method id":
			this.ccb.mb.ms.methodElementList.get((int) this.operand).printFields();break;
		case "string id":
			System.out.print("	"+this.ccb.mb.ssb.ss.stringElementList.get((int) this.operand).stringContent);break;
		case "field id" :
			break;
		default:
			break;
		}
	}
	
}