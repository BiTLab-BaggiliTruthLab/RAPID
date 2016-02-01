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
	 * Indicate a SINGLE APK or DEX file expected as the target sample file.
	 * @param apkDir Full path of the directory storing the target sample file.
	 * */
	public void setSingleApk(String apkDir){
		this.targetFile=apkDir;
	}

	
	/**
	 * If it is necessary for the user to create another directory to temporarily store the 
	 * DEX file while it is processed, this method can help setting the directory
	 * @param unzippedFileDir The string for the full path of the directory
	 * */
	public void setUnzippedFileDir(String unzippedFileDir){
		this.unzippedFileDir=unzippedFileDir;
		
	}
	/**
	 * Obtain a list of APK files detected in a specified directory.
	 * @return Null or an array list of the APK files' 'File object'.
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
	 * A 'call back' method where RAPID can receive the APIs.
	 * User has to call this method. The parameter should be an implementation for interface: 'QueryBlock',
	 * Also, function 'queries()'of the interface have to be overridden as an API container where user calls the RAPID APIs
	 * @param queryBlock the implementation of interface 'QueryBlock',
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
				try{
				if (unzippedFile!=null){
					this.dl = new DexLoader(unzippedFile);
					System.out.println("#	DEX file has been successfully loaded.");
				}else{
					throw new FileNotFoundException("ERROR:	no DEX file is found in the APK file.");
				}
				
				}catch(Exception e){
					e.printStackTrace();
					
				}
				
				
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
				System.out.println("ERROR:	no DEX file is found or APK file: "+apkFile+" is invalid.");
				e.printStackTrace();
			}
			
		}
		return unzippedFile;
	}
	/**
	 * Obtain the instructions if they invoked any one in a methods list.
	 * @param methods Target method array.
	 * @return Null or a list of instructions invoked the input methods.
	 * */
	public ArrayList<Instruction> getInsInvokeMethods(MethodElement[] methods){
		if(apiFlag==false){
			setApiLevel(3);
		}
		
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		for(MethodElement method:methods){
			
				ArrayList<Instruction> ins=getInsInvokeMethod(method);
				if(ins!=null){

					insList.addAll(ins);
				}
			
		}
		return insList;
	}
	/**
	 * Obtain the instructions that invoked a method.
	 * @param method Target methodElement object
	 * @return Null or a list of Instruction objects
	 * */
	public ArrayList<Instruction> getInsInvokeMethod(MethodElement method){
		if(apiFlag==false){
			setApiLevel(3);
		}
		
		ArrayList<MethodElement> methodList=null;
		ArrayList<Instruction> insList=new ArrayList<Instruction>();
		methodList=searchMethod(method);
		
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
			System.out.println("WARNING:	There are more than one method below matching the target. Please chose one of them.");
			for(int i=0;i<methodList.size();i++){
				methodList.get(i).printFields();	
				
			}
			
			return null;
			
		}
		
	}
	
	/**
	 * Obtain the methods including a certain function call.
	 * @param method An object of MethodElement wanted to be searched.
	 * @return Null or an array list of methods that containing the instruction(s) that invoked the method in the parameter.
	 * */
	
	public ArrayList<MethodElement> getMethodInvolker(MethodElement method) {
		ArrayList<MethodElement> methodList=null;
		ArrayList<MethodElement> returnList = new ArrayList<MethodElement>() ;
		if(apiFlag==false){
			setApiLevel(3);
		}
		methodList=searchMethod(method);
		
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
			System.out.println("WARNING:	There are more than one method below matching the target. Please chose one of them.");
			for(int i=0;i<methodList.size();i++){
				methodList.get(i).printFields();				
			}
			
			return null;
			
		}

		
		
		
	}
	/**
	 * Return true or false for the existence of a method in DEX file, 
	 * This function FULLY compares the 'attributes' of the method: 
	 * 'className', 'methodName', and 'parameterType[]',which are the 3 essential conditions
	 * @param  method Target method (an methodElement object).
	 * @return True if the method is existed in DEX file.
	 * */

	public boolean doseMethodExist(MethodElement method){
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
	 * Search method(s) in DEX file with the same fields that are not Null in the target method. 
	 * If an unique method is expected as the result, the target method should provide no null within 
	 * fields: 'className', 'methodName', and 'parameterType[]'.
	 * If all attributes are NULL, the return is the whole list of methods.
	 * @param method Target method (methodElement object).
	 * @return A list of method(s) (methodElement object).
	 * */
	public ArrayList<MethodElement> searchMethod(MethodElement method){
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
	 * Return True if a string can fully match a string parsed from DEX file
	 * @param keyword The string wanted to be searched
	 * @return True if it exists.
	 * */
	public boolean doseStringExist(String keyword) {
		//setApiLevel(0);
		for(int i = 0;i<this.stringComponent.stringElementList.size();i++){
			if(this.stringComponent.stringElementList.get(i).stringContent.equals(keyword)){
				
				return true;
			}
			
		}
		return false;
		
	}
	/**
	 * Search a string in DEX file. 
	 * @param stringContent string for searching.
	 * @return a list of matched string (stringElements object).
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
	 * Search string(s) in DEX file that contains the input string. 
	 * @param keyword Target string.
	 * @return An array list of eligible string(s) (stringElement object).
	 * */
	public ArrayList<StringElement> searchStringContaines(String keyword) {
		ArrayList<StringElement> stringList = new ArrayList<StringElement>();
		for(int i = 0;i<this.stringComponent.stringElementList.size();i++){
			if(this.stringComponent.stringElementList.get(i).stringContent.contains(keyword)){
				stringList.add(this.stringComponent.stringElementList.get(i));
				
			}			
		}
		return stringList;
				
	}
	/**
	 * Obtain a list of method objects parsed out of a DEX file.
	 * @return Null or an array list of 'MethodElement' objects.
	 * The list includes methods with a definition and a code block and 
	 * methods without those, which is also considered as an API in RAPID.
	 * */
	public ArrayList<MethodElement> getMethodList() {
		if(apiFlag==false){
			setApiLevel(2);
		}
		return this.methodComponent.methodElementList;
		
	}
	/**
	 * Obtaining the strings parsed out of a DEX file
	 * @return Null or an array list of 'StringElement' objects.
	 * 'String' objects here represent all strings that exist in the application, e.g., 
	 *  values in string variables, method or class names, function return values etc.
	 * */
	public  ArrayList<StringElement> getStringList(){
		
		return this.stringComponent.stringElementList;
	
	}
	/**
	 * Obtain a list of all utilized APIs in a DEX file.
	 * Term 'API' in RAPID represents all the methods utilized in a DEX file without a code block.
	 * @return Null or the array list where contains the 'MethodElement' instances of the eligible methods.
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
	 * Search an instructions by matching the opcode and the operand.
	 * @param opcode the string type of opcode that one instruction must has. 
	 * @param operand the number of operand, sometimes referring to string id, 
	 * method id or others depending on the type of operand.
	 * @return Null or the eligible list of instructions (Instruction objects).
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
	 * Search an instructions with same opcode
	 * @param opcode the string type of opcode
	 * @return The Null or the list of matched instructions (Instruction objects).
	 * */
	public ArrayList<Instruction> searchInsWithOpc(String opcode){
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
	 * Search instructions of a DEX file in case they can match any one in an instruction list.
	 * Fields 'opcode' and 'operand' are checked maximum if the value of these two fields are not NULL.
	 * @param targetIns Target instruction list.
	 * @return NULL when there is no matching instructions or an instruction list (Instruction objects).
	 * */
	public ArrayList<Instruction> searchInstruction(Instruction[] targetIns){
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
	 * Obtain a list of class names in DEX file.
	 * @return Null or an array list of class names.
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
	 * Obtain a list of code blocks parsed out of DEX file.
	 * @return Null or an array list of CodeBlock objects.
	 * */
	public 	ArrayList<CodeBlock> getCodeBlockList(){
		if(apiFlag==false){
			setApiLevel(2);			
		}
		return this.codeBlockComponent.codeBlockList;
	}
	/**
	 * Obtain the codeBlock of a method with certain method Id.
	 * @return Null or a codeBlock element.
	 * */
	public 	CodeBlock getCodeBlockById(int methodId){
		if(apiFlag==false){
			setApiLevel(2);			
		}
		return this.methodComponent.methodElementList.get(methodId).codeBlock;
	}
	/**
	 * Obtain the instructions that invoked external files (JAR, DEX or SO).
	 * @return Null or an instruction(s) list that invoked the APIs below in a DEX file.
	 * java.lang.System.load(..)
	 * java.lang.System.loadLibrary(..)
	 * dalvik.system.DexClassLoader.DexClassLoader(..)
	 * dalvik.system.PathClassLoader.PathClassLoader(..)
	 * */
	public ArrayList<Instruction> getInsLoadExternalFiles(){
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
		
			insList=this.getInsInvokeMethods(ins);
			
			return insList;
		
	}
	/**
	 * Obtain pairs of values
	 * 1. The address of the instructions for loading external files / libraries
	 * 2. The back-traced static value referring to the directory saving the external files.	
	 * @return Map <Long,String> Long: static value;	String: address	of the instruction.	
	 * */
	public Map <Long,String> getExternalFilesDirs(){
		Map <Long,String>  addressAndDir=new HashMap <Long,String> ();
		
		if(apiFlag==false){
			setApiLevel(3);			
		}
		ArrayList<Instruction> insList=getInsLoadExternalFiles();
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
	 * Return true if any instruction called external files.
	 * @return Return TRUE if APIs below are invoked.
	 * java.lang.System.load(..)
	 * java.lang.System.loadLibrary(..)
	 * dalvik.system.DexClassLoader.DexClassLoader(..)
	 * dalvik.system.PathClassLoader.PathClassLoader(..)
	 * */
	public boolean areExternalFilesloaded(){
		if(apiFlag==false){
			setApiLevel(3);			
		}		
			if(getInsLoadExternalFiles()!=null){
				return true;
			}else
			
			return false;
	}
	/**
	 * Return true if an certain API is exist
	 * @param api An MethodElement object
	 * @return True if the API is exist
	 **/
	public boolean doseApiExist(MethodElement api){
		
		if(apiFlag==false){
			setApiLevel(2);
		}

		ArrayList<MethodElement> apiList = getApiList();
		if(apiList!=null){
			for(MethodElement me:apiList){
				if(api.className!=null){
					if(api.className != api.methodName)
						return false;
				}
				
				if(api.methodName != me.methodName)
						return false;
				
				if(api.parameterType!=null){
					if(api.parameterType == me.parameterType)
						return false;
				}
				if(api.returnValueType!=null){
					if(api.returnValueType == api.returnValueType)
						return false;
				}
				return true;	
			}
		}
		return false;
		
		
	}
	/**
	 * Return true if a method is invoked in the target DEX file.
	 * @param targetMethod Target method (MethodElement object).
	 * @return True if the target method is invoked.
	 * */
	public boolean isMethodInvoked(MethodElement targetMethod){
		
		if (null!=getInsInvokeMethod(targetMethod))
			return true;
		else 
			return false;
		
	}
	/**
	 * Search instructions where a certain string is assigned
	 * @param stringContent target string
	 * @return An array list of instructions (Instruction objects) with the string.
	 * */
	public 	ArrayList<Instruction> searchInsWithString(String stringContent){
		
		if(apiFlag==false){
			setApiLevel(3);
		}
		StringElement se = this.searchString(stringContent);
		if (se==null){
			return null;
		}else{
			ArrayList<Instruction> insList = new ArrayList<Instruction>();
				for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
					for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
						Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
					
							if(ins.opcode.equals("const-string")&&ins.opcode.equals("const-string/jumbo")){
								if(ins.getOperand()==se.stringId){
									insList.add(ins);	
								}
													
							}						
					}
				}
				return insList;
			
			}
		}
	/**
	 * Search instructions by a stringElement. An overloaded method of function: searchInsWithString(String))
	 * @param string Target stringElement for searching
	 * @return Null or an array list of instructions related to the target string
	 * */
	
	public 	ArrayList<Instruction> searchInsWithString(StringElement string){
		
		if(apiFlag==false){
			setApiLevel(3);
		}

			ArrayList<Instruction> insList = new ArrayList<Instruction>();
				for(int i = 0;i<this.codeBlockComponent.codeBlockList.size();i++){
					for(int j = 0;j<this.codeBlockComponent.codeBlockList.get(i).instructionList.size();j++){
						Instruction ins = this.codeBlockComponent.codeBlockList.get(i).instructionList.get(j);
					
							if(ins.opcode.equals("const-string")&&ins.opcode.equals("const-string/jumbo")){
								if(ins.getOperand()==string.stringId){
									insList.add(ins);	
								}
													
							}						
					}
				}
				return insList;
			
	}
		
		
}