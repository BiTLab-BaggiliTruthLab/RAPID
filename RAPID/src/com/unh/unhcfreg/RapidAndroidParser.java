package com.unh.unhcfreg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;




import java.util.HashMap;
import java.util.Map;

import dataComponent.CodeBlock;
import dataComponent.CodeComponent;
import dataComponent.Instruction;
import dataComponent.MethodComponent;
import dataComponent.MethodElement;
import dataComponent.StringComponent;
import dataComponent.StringElement;
import systemComponent.DexLoader;
import systemComponent.MethodComponentBuilder;
import systemComponent.CodeComponentBuilder;
import systemComponent.StringComponentBuilder;
import systemComponent.ZipDecompressor;
/**
 * @author xiaolu
 * Create a RAPID tool instance
 * */
public class RapidAndroidParser {
	private  String apkDir=System.getProperty("user.dir");
	private  ArrayList<File> apkList=new ArrayList<File>();
	private  String unzippedFileDir;
	private  DexLoader dl;
	private  StringComponent stringComponent;
	private  MethodComponent methodComponent;
	private  CodeComponent codeBlockComponent;
	private  String targetFile=null;

	private  int apiLevel=0;//0->string
							//1->method definition
							//2->method with code block information
							//3->code block with parsed instruction
	private boolean apiFlag=false;
	/**
	 * Customizing the directory for storing the APK files or DEX files. 
	 * The name list of APK files under this directory will be detected by RAPID.
	 * @param apkDir the string of the full path. 
	 * */
	public void setApkDir(String apkDir){
		this.apkDir=apkDir;
		
	}
	/**
	 * Indicate a certain file being the target file which can be APK or DEX file.
	 * */
	public void processSingleFile(String apkDir){
		this.targetFile=apkDir;
	}

	
	/**
	 * If it is necessary for the user to create another directory to store the 
	 * temporary DEX file while it is being processed, this method can set the directory
	 * @param unzippedFileDir The string for the full path of the directory
	 * */
	public void setUnzippedFileDir(String unzippedFileDir){
		this.unzippedFileDir=unzippedFileDir;
		
	}
	/**
	 * Obtaining the list of APK files' objects
	 * @return Return an array list of the APK files' object
	 * */
	public ArrayList<File> getApkList(){
		return apkList;
	}

	private void setApiLevel(int level){
		if(level>this.apiLevel){
			this.apiLevel=level;
		}
	}
	private void setApkList(){
		
        File dir = new File(this.apkDir);
        
        FilenameFilter searchSuffix = new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(".apk")||name.endsWith(".dex")){
                	return true;  
                }else{
                	return false;
                }
            	      
            }
        };
        
        File []files = dir.listFiles();
        if (files.length==0){
        	try {
				throw new FileNotFoundException("WARNING:	No 'APK' file found in directory: "+this.apkDir+".");
			} catch ( FileNotFoundException e) {
				// TODO Auto-generated catch block				
				e.printStackTrace();
			}
        }
        for(File f : files){
                if(searchSuffix.accept(dir, f.getName())){
                    apkList.add(f);
                }
        }

	}

	/**
	 * Call back method where RAPID can receive the APIs called by user.
	 * User has to call this method and implement the interface of 'QueryBlock' as the parameter,
	 * @param queryBlock the implementation of interface 'QueryBlock',
	 * The function 'queries()'of the interface should be override as a APIs container.
	 * */	
	public void setQuery(QueryBlock queryBlock) {
		System.out.println("#	RAPID version 0.01: a forensic examnation tool for DEX file ");
		setApkList();
		String unzippedFile;
		if(this.targetFile!=null){
				if(this.targetFile.endsWith(".apk")){
					File targetFile=new File(this.targetFile);
					unzippedFile=unzipApk(targetFile);
					System.out.println("#	DEX file is successfully decompressed from APK file: "+unzippedFile+".");
				}else{
					unzippedFile=this.targetFile;
				}
				loadIntoMemory(unzippedFile);
				instructionLevelQuary();
				System.out.println("#	Data Components has been constructed.");
				System.out.println("#	Performing queries.....");
				System.out.println("");
				queryBlock.queries();
				System.out.println("");
				dl.cleanBuffer();
				
		} else{
				for (int i=0;i<apkList.size();i++){
					
		
					File currentApk = apkList.get(i);
					if(currentApk.getName().endsWith(".apk")){
						unzippedFile=unzipApk(currentApk);
						System.out.println("#	DEX file is successfully decompressed from APK file: "+currentApk+".");
					}else{
						System.out.println("#	DEX file "+currentApk.getName()+" is ready");
						unzippedFile=currentApk.getAbsolutePath();
					}
		
					loadIntoMemory(unzippedFile);
					System.out.println("#	DEX file has been successfully loaded.");
					if (0==i){
							instructionLevelQuary();
					}else{
						switch (apiLevel){
							case 0: stringLevelQuary();
									break; 
							case 1: methodLevelQuary();
									break; 
							case 2: codeLevelQuary();
									break; 
							case 3: instructionLevelQuary();
									break;
						}
					}
				
					System.out.println("#	Data Components has been constructed.");
					System.out.println("#	Performing queries.....");
					System.out.println("");
				
					queryBlock.queries();
					
					dl.cleanBuffer();
				}
		}
	}

	private void stringLevelQuary() {
				StringComponentBuilder scb = new StringComponentBuilder(dl.mbb);
				this.stringComponent = scb.ss;			
	}
	private void methodLevelQuary() {
				MethodComponentBuilder mcb=new MethodComponentBuilder(dl.mbb);
				this.stringComponent = mcb.ssb.ss;
				this.methodComponent = mcb.ms;
	}
	private void codeLevelQuary() {
				CodeComponentBuilder ccb= new CodeComponentBuilder(dl.mbb);
				this.stringComponent = ccb.mb.ssb.ss;
				this.methodComponent =ccb.mb.ms;
				this.codeBlockComponent = ccb.codeComponent;
				
	}
	private void instructionLevelQuary() {
				CodeComponentBuilder ccb= new CodeComponentBuilder(dl.mbb);
				this.stringComponent = ccb.mb.ssb.ss;
				this.methodComponent =ccb.mb.ms;
				this.codeBlockComponent = ccb.codeComponent;
				ccb.loadInstructionToCodeBlock();		
	}

	private void loadIntoMemory(String unzippedFile){

				this.dl = new DexLoader(unzippedFile);

	}
	/**
	 * Unzipping a classes.dex out from a APK file
	 * */
	private String unzipApk(File apkFile) {
		String unzippedFile = null;
		if(apkFile.exists()){
			ZipDecompressor zd=new ZipDecompressor();	
			zd.setApkDir(apkFile.getAbsolutePath(),unzippedFileDir);
			try {
				unzippedFile=zd.unzipFile("classes.dex");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("WARNING:	no DEX file found or APK file: "+apkFile+" is invalid.");
				e.printStackTrace();
			}
			
		}
		return unzippedFile;
	}
	/**
	 * Obtaining the instructions invoking certain methods.
	 * @param methods Target method array, instance of MethodElement.
	 * @return instructions founded that invoke the input methods
	 * */
	public ArrayList<Instruction> getInvokedInstruction(MethodElement[] methods){
		if(apiFlag==false){
			setApiLevel(3);
		}
		
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		for(MethodElement method:methods){
			
				ArrayList<Instruction> ins=getInvokedInstruction(method);
				if(ins!=null){

					insList.addAll(ins);
				}
			
		}
		return insList;
	}
	/**
	 * Obtaining the instructions invoking a certain method.
	 * @param method Target method, instance of MethodElement.
	 * */
	public ArrayList<Instruction> getInvokedInstruction(MethodElement method){
		if(apiFlag==false){
			setApiLevel(3);
		}
		
		ArrayList<MethodElement> methodList=null;
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		methodList=searchSimilarMethod(method);
		
		if(methodList.size()==1){
			for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
				for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
					Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
					if((ins.opcode=="invoke-kind"||ins.opcode=="invoke-kind/range")&&methodList.get(0).methodId==ins.getOperand()){

						insList.add(ins);
						
					}
				}
			}
			return insList;
		}else{
			if (methodList.size()==0){
				System.out.print("There is no method matching the target method: ");
				method.printFields();
				
				return null;
			}
			System.out.println("There are more than one method below matching the target. Please chose one of them.");
			for(int i=0;i<methodList.size();i++){
				methodList.get(i).printFields();	
				
			}
			
			return null;
			
		}
		
	}
	
	/**
	 * Obtaining all the methods that have called a assigned method.
	 * @param method An instance of class 'MethodElement'.
	 * @return The array list of the methods that contains the instruction(s) invoking the parameter method.
	 * */
	
	public ArrayList<MethodElement> getMethodInvolker(MethodElement method) {
		ArrayList<MethodElement> methodList=null;
		ArrayList<MethodElement> returnList = new ArrayList<MethodElement>() ;
		if(apiFlag==false){
			setApiLevel(3);
		}
		methodList=searchSimilarMethod(method);
		
		if(methodList.size()==1){
			
					for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
						for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
							Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
							if((ins.opcode=="invoke-kind"||ins.opcode=="invoke-kind/range")&&methodList.get(0).methodId==ins.getOperand()){
								
									int methodId=this.codeBlockComponent.codeBlockList.get(i).methodId;
									MethodElement involkerMethod=this.methodComponent.methodElementList.get(methodId);	
									returnList.add(involkerMethod);
								
							}
						}
					}
					return returnList;
		}else{
			if (methodList.size()==0){
				System.out.print("There is no method matching the target method:");
				method.printFields();
				return null;
			}
			System.out.println("There are more than one method below matching the target. Please chose one of them.");
			for(int i=0;i<methodList.size();i++){
				methodList.get(i).printFields();				
			}
			
			return null;
			
		}

		
		
		
	}
	/**
	 * Returning true or false for the existence of a certain method in DEX file, 
	 * which FULLY compares the attributes: 
	 * 'className', 'methodName', and 'parameterType[]',which are the 3 essential conditions
	 * for affirming an independent method.
	 * @param  method Target method object.
	 * */

	public boolean isMethodExist(MethodElement method){
		if(apiFlag==false){
			setApiLevel(1);
		}

			for(int i=0;i<this.methodComponent.methodElementList.size();i++){
				MethodElement me=this.methodComponent.methodElementList.get(i);
				if(me.className==method.className
				 &&me.methodName==method.methodName
				 &&me.parameterType==method.parameterType){
					return true;
				}
				else{
					if(method.parameterType!=null){							
						if (!Arrays.equals(me.parameterType,method.parameterType)){
							continue;
						}
					}
					if(method.className!=null){
						if (!me.className.equals(method.className)){
							continue;
						}
					}
					if(method.methodName!=null){
						if (!me.methodName.equals(method.methodName)){
							continue;
						}	
					}
					return true;
				} 
			}
			return false;			
	}
	/**
	 * Searching similar method(s) with the parameter
	 * Comparing the attributes: 'className', 'methodName', and 'parameterType[]' maximum.
	 * It is not going to be check, If an attributes is NUll. 
	 * If all attributes are NULL, the return is the whole list of methods.
	 * @return Returning the method list with non-NULL value attributes matched. 
	 * */
	public ArrayList<MethodElement> searchSimilarMethod(MethodElement method){
		if(apiFlag==false){
			setApiLevel(2);
		}
		
		ArrayList<MethodElement> methodList=new ArrayList<MethodElement>();

				
					for(int i=0;i<this.methodComponent.methodElementList.size();i++){
						
						MethodElement me=this.methodComponent.methodElementList.get(i);
						if(method.parameterType!=null){							
							if (!Arrays.equals(me.parameterType,method.parameterType)){
								continue;
							}
						}
						if(method.className!=null){
							if (!me.className.equals(method.className)){
								continue;
							}
						}
						if(method.methodName!=null){
							if (!me.methodName.equals(method.methodName)){
								continue;
							}
						}	
						
						/*if (me.parameterNumber!=method.parameterNumber){
								continue;
						}*/
						
						
							methodList.add(me);
						
					}
				
		
		return methodList;
		
	}
	
	
	/**
	 * Return True if the assigned string can fully match a string in DEX file
	 * @return boolean value
	 * */
	public boolean isStringExist(String keyword) {
		//setApiLevel(0);
		for(int i = 0;i<this.stringComponent.stringElementList.size();i++){
			if(this.stringComponent.stringElementList.get(i).stringContent.equals(keyword)){
				
				return true;
			}
			
		}
		return false;
		
	}
	/**
	 * Obtaining the StringElement object 
	 * if the assigned string can fully match the object鈥榮 'stringContent' field銆�
	 * */
	public StringElement searchString(String stringContent){
		for(int i = 0;i<this.stringComponent.stringElementList.size();i++){
			if(this.stringComponent.stringElementList.get(i).stringContent.equals(stringContent)){
				
				return this.stringComponent.stringElementList.get(i);
			}
			
		}
		return null;
	}
	/**
	 * Obtaining a StringElement object list if the assigned string can be part of their 'stringContent' in DEX file.
	 * @return Array list of eligible string object(s).
	 * */
	public ArrayList<StringElement> stringContaines(String keyword) {
		ArrayList<StringElement> stringList = new ArrayList<StringElement>();
		for(int i = 0;i<this.stringComponent.stringElementList.size();i++){
			if(this.stringComponent.stringElementList.get(i).stringContent.contains(keyword)){
				stringList.add(this.stringComponent.stringElementList.get(i));
				
			}			
		}
		return stringList;
				
	}
	/**
	 * Obtaining the whole list of methods in a DEX file,
	 * which includes methods having a definition in DEX file and 
	 * methods only invoked in DEX file, which is also considered as APIs in RAPID
	 * @return the array list of 'MethodElement' instances
	 * */
	public ArrayList<MethodElement> getMethodList() {
		if(apiFlag==false){
			setApiLevel(2);
		}
		return this.methodComponent.methodElementList;
		
	}
	/**
	 * Obtaining the while list of strings in a DEX file
	 * 'String' objects here represent all strings that exist in the application, e.g., 
	 *  values in string variables, method or class names, function return values etc.
	 * @return the array list of 'StringElement' instances
	 * */
	public  ArrayList<StringElement> getStringList(){
		
		return this.stringComponent.stringElementList;
	
	}
	/**
	 * Obtaining the list of all utilized APIs in a DEX file
	 * Term 'API' in RAPID is represented by all the methods utilized in a DEX file without definition and code block.
	 * @return the array list containing the 'MethodElement' instances of the eligible methods
	 * */
	public ArrayList<MethodElement> getApiList(){
		
		if(apiFlag==false){
			setApiLevel(2);
		}
		ArrayList<MethodElement> apiList = new ArrayList<MethodElement>();
		
		
		for(int i=0;i<methodComponent.methodElementList.size();i++){
			MethodElement me=methodComponent.methodElementList.get(i);
			if(me.hasCodeBlock==false){
				
				apiList.add(me);
			}
		}
		
		return apiList;
	}
	/**
	 * Searching certain instructions by matching its opcode and the operand.
	 * The return value can be NULL when there is no matching instructions.
	 * @return The eligible instructions
	 * */
	public ArrayList<Instruction> searchInstruction(String opcode,long operand){
		if(apiFlag==false){
			setApiLevel(3);
		}
		ArrayList<Instruction> insList = new ArrayList<Instruction>();
			for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
				for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
					Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
					if(ins.hasOperand==true){
						if(ins.opcode.equals(opcode)&&operand==ins.getOperand()){

							insList.add(ins);
						
						}
					}
					
				}
			}
			return insList;
		
	}
	/**
	 * Searching certain instructions by only checking its opcode
	 * The return value can be NULL when there is no matching instructions.
	 * */
	public ArrayList<Instruction> searchInstruction(String opcode){
		if(apiFlag==false){
			setApiLevel(3);
		}
		ArrayList<Instruction> insList = new ArrayList<Instruction>();
			for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
				for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
					Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
				
						if(ins.opcode.equals(opcode)){

							insList.add(ins);						
						}						
				}
			}
			return insList;
		
	}
	/**
	 * Obtaining instructions through searching a Instruction list.
	 * Fields 'opcode' and 'operand' are checked maximum if the value of them is not NULL.
	 * The return value can be NULL when there is no matching instructions.
	 * */
	public ArrayList<Instruction> searchInstructions(Instruction[] targetIns){
		if(apiFlag==false){
			setApiLevel(3);
		}
		

		ArrayList<Instruction> insList = new ArrayList<Instruction>();
			for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
				for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
					Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);	
					for(Instruction instruction: targetIns){
						if(instruction.opcode.equals(ins.opcode)){
							if(instruction.hasOperand==false){
								insList.add(ins);
							}else if(instruction.getOperand()==ins.getOperand()){
								insList.add(ins);
							}
							
									
						} 
					}
				}
			}
			return insList;
		
	}
	/**
	 * Obtain the list of classes in DEX file
	 * */
	public ArrayList<String> getClassList(){		
		if(apiFlag==false){
			setApiLevel(1);
		}
		
		ArrayList<String> classList=new ArrayList<String>();
		for(int i=0;i<this.methodComponent.methodElementList.size();i++){
			MethodElement me=methodComponent.methodElementList.get(i);
			if(!classList.contains(me.className)){				
				classList.add(me.className);				
			}
		}
		return classList;
		
	}
	/**
	 * Obtain the code block list
	 * */
	public 	ArrayList<CodeBlock> getCodeBlockList(){
		if(apiFlag==false){
			setApiLevel(2);			
		}
		return this.codeBlockComponent.codeBlockList;
	}
	/**
	 * Obtain the codeBlock of a method with method Id
	 * */
	public 	CodeBlock getCodeBlock(int methodId){
		if(apiFlag==false){
			setApiLevel(2);			
		}
		return this.methodComponent.methodElementList.get(methodId).codeBlock;
	}
	/**
	 * Search instructions invoking external files (JAR, DEX or SO).
	 * Back trace the the path of the external files If it exist in DEX file statically
	 * @return return the instruction(s) where the APIs below is invoked in DEX file.
	 * java.lang.System.load(..)
	 * java.lang.System.loadLibrary(..)
	 * dalvik.system.DexClassLoader.DexClassLoader(..)
	 * dalvik.system.PathClassLoader.PathClassLoader(..)
	 * */
	public ArrayList<Instruction> getInstructionsOfExternalFiles(){
		if(apiFlag==false){
			setApiLevel(3);			
		}
		
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		
		MethodElement[] ins=new MethodElement[5];
		ins[0]=new MethodElement ("java.lang.System","load",null,null);
		
		ins[1]=new MethodElement ("java.lang.System","loadLibrary",null,null);
		
		ins[2]=new MethodElement ("dalvik.system.DexClassLoader","<init>",null,null);
		
		ins[3]=new MethodElement ("dalvik.system.PathClassLoader","<init>",null,new String[]{"java.lang.String","java.lang.ClassLoader"});
		
		ins[4]=new MethodElement ("dalvik.system.PathClassLoader","<init>",null,new String[]{"java.lang.String","java.lang.String","java.lang.ClassLoader"});
		
			insList=this.getInvokedInstruction(ins);
			
			return insList;
		
	}
	/**
	 * Obtain pairs of values
	 * 1. The address of the instructions for loading external files / libraries
	 * 2. The static value found for the directory where loads the files while DEX file running.	
	 * @return Map <Long,String> Long: static value found;	String: address		
	 * */
	public Map <Long,String> getExternalFilesDirectory(){
		Map <Long,String>  addressAndDir=new HashMap <Long,String> ();
		
		if(apiFlag==false){
			setApiLevel(3);			
		}
		ArrayList<Instruction> insList=getInstructionsOfExternalFiles();
		for(Instruction ins:insList){
			if(ins!=null){
				String dir=ins.staticBackTrace("STRING",0);
			
				long address=ins.address;
				addressAndDir.put(address,dir);
			}
			
			
		}
		
		return addressAndDir;
	}
	/**
	 * Search instructions invoking external files
	 * @return Return TRUE 
	 * */
	public boolean areExternalFilesLoad(){
		if(apiFlag==false){
			setApiLevel(3);			
		}		
			if(getInstructionsOfExternalFiles()!=null){
				return true;
			}else
			
			return false;
	}
}