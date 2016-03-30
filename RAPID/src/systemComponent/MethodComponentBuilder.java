package systemComponent;

import java.nio.MappedByteBuffer;


import dataComponent.MethodElement;
import dataComponent.MethodComponent;

/**
 *  Method Component construction
 * */
public class MethodComponentBuilder extends ComponentBuilder{
	/**creating a StringComponentBuilder for using strings stored in String Component*/
	public StringComponentBuilder ssb;
	/**creating an Method Component*/
	public MethodComponent ms= new MethodComponent();
	/**A construction method that calls the method in father class 'MethodComponent', 
	 * which let Method Component finding the DEX file in memory
	 * @param mbb DEX file loaded in memory
	 * */
	public MethodComponentBuilder(MappedByteBuffer mbb) {
		super(mbb);
		// TODO Auto-generated constructor stub
		buildComponent();
	}
	private void buildComponent() {
		getSectionOffsets();
		ssb = new StringComponentBuilder(mbb);
		
		readBinary(0x58,4,"ADDRESS");//Method_ids
			long size=tempValue;
		readBinary(0x5c,4,"ADDRESS");//Method_ids
			long stratAddress=tempValue;

		for(int i=0;i<size;i++){
			MethodElement me = new MethodElement();
			me.methodId=i;
			me.address=stratAddress;
			getProtoType(me);
			getClassName(me);
			getMethodName(me);
			ms.methodElementList.add(me);			
			stratAddress=stratAddress+8;
		}
	}
	private void getMethodName(MethodElement me){
		readBinary(me.address+4,4,"ADDRESS");
		long methodNameStringId=tempValue;	
		me.methodName=ssb.ss.stringElementList.get((int)methodNameStringId).stringContent;//Method_ids
		
	}
	private void getClassName(MethodElement me){
		
		readBinary(me.address,2,"ADDRESS");//classNameId
			long classNameId=tempValue;		
		readBinary(0x40,4,"ADDRESS");
			long size =tempValue;						
		readBinary(0x44,4,"ADDRESS");
			long stratAddress =tempValue;			
		readBinary(stratAddress+(classNameId*4),4,"ADDRESS");//Type_ids
			long classStringId=tempValue;
			
				String cName= ssb.ss.stringElementList.get((int)classStringId).stringContent;
						
				me.className=typeTranslate(cName);
		
	}
	private void getProtoType(MethodElement me){
		readBinary(me.address+2,2,"ADDRESS");
		long protoId=tempValue;
		readBinary(0x4c,4,"ADDRESS");
		long protoIdStart=tempValue;
		readBinary(protoIdStart+(protoId*12)+4,4,"ADDRESS");	
		long returnTypeStringId=tempValue;
		
		readBinary(0x44,4,"ADDRESS");
		long typeIdStrat=tempValue;
		readBinary(typeIdStrat+4*returnTypeStringId,4,"ADDRESS");
		returnTypeStringId=tempValue;
		me.returnValueType=typeTranslate(ssb.ss.stringElementList.get((int)returnTypeStringId).stringContent);//return value obtained
		
		readBinary(protoIdStart+(protoId*12)+4+4,4,"ADDRESS");
		long parametersOffset=tempValue;
		if(parametersOffset!=0){
				readBinary(parametersOffset,4,"ADDRESS");
				int parameterNumber=(int) tempValue;			
				me.parameterType=new String[parameterNumber];			
				parametersOffset=parametersOffset+4;			
				for(int i=0;i<parameterNumber;i++){
					readBinary(parametersOffset+(2*i),2,"ADDRESS");				
					long parameterMethodId=tempValue;
					
					readBinary(typeIdStrat+4*parameterMethodId,4,"ADDRESS");
					parameterMethodId=tempValue;
					me.parameterType[i]=typeTranslate(ssb.ss.stringElementList.get((int)parameterMethodId).stringContent);
				}
			
		}

	}
	private String typeTranslate(String rawType){
		int arrayLevel=0;
		String readableType = rawType;
		while(readableType.startsWith("[")){
			readableType=readableType.substring(1,readableType.length());
			arrayLevel++;
		}
		if(readableType.startsWith("V")){
			readableType="void";
		}
		if(readableType.startsWith("Z")){
			readableType="boolean";
		}
		if(readableType.startsWith("B")){
			readableType="byte";
		}
		if(readableType.startsWith("S")){
			readableType="short";
		}
		if(readableType.startsWith("C")){
			readableType="char";
		}
		if(readableType.startsWith("I")){
			readableType="int";
		}
		if(readableType.startsWith("J")){
			readableType="long ";
		}
		if(readableType.startsWith("F")){
			readableType="float";//(64bit)
		}
		if(readableType.startsWith("D")){
			readableType="double";//(64bit)
		}

		if(readableType.startsWith("L")){
			readableType=readableType.substring(1,readableType.length()).replaceAll("/", ".").replaceAll(";", "");
		}		
		for(int i=0;i<arrayLevel;i++){
			readableType=readableType+"[]";
		}
		
		return readableType;
	}
}
