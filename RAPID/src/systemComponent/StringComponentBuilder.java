package systemComponent;


import java.nio.MappedByteBuffer;


import dataComponent.StringElement;
import dataComponent.StringComponent;

/**
 *  String Component construction
 * */
public class StringComponentBuilder extends ComponentBuilder{
	/**creating an String Component*/
	public StringComponent ss= new StringComponent();
	/**A construction method that calls the method in father class 'MethodComponent', 
	 * which let String Component finding the DEX file in memory
	 * @param mbb DEX file loaded in memory
	 * */
	public StringComponentBuilder(MappedByteBuffer mbb){
		super(mbb);
		// TODO Auto-generated constructor stub
		buildComponent();
		
	}
	private void parseStringName(StringElement se){
		readBinary(se.address,se.stringLength,"ASCII");
		se.stringContent=tempString;
		//return this.stringName;
	}
	private void buildComponent() {
		
		
		readBinary(0x38,4,"ADDRESS");
		long size=tempValue;
		readBinary(0x3c,4,"ADDRESS");
		long startAddress=tempValue;//0x70
		int count=0;	
			while(count<size){
				StringElement se=new StringElement();
				
				readBinary(startAddress,4,"ADDRESS");				
				long stringAddress=tempValue;	
				readBinary(stringAddress,1,"VALUE");
				getUleb(stringAddress,tempValue);				
				long ulebLength=tempUlebByteNum;
				
				long stringLength=tempUlebValue;
				se.stringId=count;
				se.address=stringAddress+ulebLength;
				se.stringLength=stringLength;
				
				parseStringName(se);
				ss.stringElementList.add(se);
				startAddress=startAddress+4;
				
				count++;

			}
					
	}

}
