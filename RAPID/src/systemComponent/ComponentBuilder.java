package systemComponent;

import java.nio.MappedByteBuffer;
/**father class for Builders,
 * which contains general-use binary code operations for the components construction.
 * */
public class ComponentBuilder {
	protected MappedByteBuffer mbb;
	/**this field temporarily stores the value result of the public functions in this class*/
	public long tempValue;
	/**this field temporarily stores the string result of the public functions in this class*/
	public String tempString;
	
	//sectionOffsetList extracted from map section
	protected long mapOffset;
	protected long sectionNum;
	protected long stringDataStart;
	protected long stringDataSize;
	protected long ClassDataStart;
	protected long ClassDataSize;
	
	protected long tempUlebByteNum;
	protected long tempUlebValue;
	
	/**A construction method that let Components find the DEX file in memory
	 * */
	public ComponentBuilder(MappedByteBuffer mbb) {
		
		setMappedByteBuffer(mbb);				
	}
	private void setMappedByteBuffer(MappedByteBuffer mbb){
		this.mbb=mbb;
	}
	
	/**
	 * obtaining the offset for several sections of DEX file
	 * */
	public void getSectionOffsets(){
				
		readBinary(0x34,4,"ADDRESS");		
			this.mapOffset=tempValue;//map section offset
		readBinary(mapOffset,4,"ADDRESS");	
			this.sectionNum=tempValue;
			long checkStart=mapOffset+4;
			long endOfMap =((this.sectionNum*12)+this.mapOffset);
		while(checkStart<endOfMap){
			readBinary(checkStart,2,"ADDRESS");
				long sectionId=tempValue;
				if(sectionId==0x2002){
					readBinary(checkStart+8,4,"ADDRESS");
					this.stringDataStart=tempValue;					
					readBinary(checkStart+4,4,"ADDRESS");
					this.stringDataSize=tempValue;
				}
				if(sectionId==0x2000){
					readBinary(checkStart+8,4,"ADDRESS");
					this.ClassDataStart=tempValue;					
					readBinary(checkStart+4,4,"ADDRESS");
					this.ClassDataSize=tempValue;
				}
				checkStart=checkStart+12;
		}
	}
	/**
	 * 'uleb' coding identification
	 * @param stringAddress The offset of the string coded by 'uleb' coding.
	 * @param firstByte The content of the first byte
	 * */
	public void getUleb(long stringAddress,long firstByte){
		long preUleb=firstByte;
		int i=1;
		while(firstByte>0x7f){
			
			stringAddress++;
			readBinary(stringAddress,1,"VALUE");
			preUleb=(tempValue<<(8*i))+preUleb;
			firstByte=tempValue;
			i++;
		}
		
		long finalUleb=getUlebValue(preUleb);
		this.tempUlebValue=finalUleb;
		this.tempUlebByteNum=i;
	}
	
	private long getUlebValue(long preUleb) {
						
		long newOx = 0;
		int i=0;
		while(preUleb > 0x7f){			
				long temp = preUleb & 0x7f;			
				temp = temp<<7*i;
				preUleb=preUleb>>8;			
				newOx=newOx+temp;
				i++;
				}
				preUleb = preUleb << 7*i;
				newOx = preUleb + newOx;
								
		return newOx;
	}
	/**
	 * read the binary code with certain length and certain offset. 
	 * The result will be stored in member variables: 'tempValue' or 'tempString'
	 * @param mapOffset offset of a binary code
	 * @param stringLength length of a binary code
	 * @param returnValueType the string indicates that the return of this function is an "ADDRESS", "ASCII" or "VALUE"
	 * */					
	public void readBinary(long mapOffset,long stringLength,String returnValueType){
		
		if(returnValueType=="ADDRESS"){
			try {
				binaryProcess(mapOffset,stringLength);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("£¡£¡	Reading binary code failed, please check if the DEX file is valid.");
				e.printStackTrace();
			}
			tempString=highToLow(tempValue);
			
			int length = tempString.length();
			if(stringLength==4){
				if(length!=8){
					for(int i=0;i<8-length;i++){
						tempString=tempString+"0";
					}
				}
			}else if(stringLength==2){
				if(length!=4){
					for(int i=0;i<4-length;i++){
						tempString=tempString+"0";
					}
				}
			}if(stringLength==1){
				if(tempString=="0"){
					tempString="00";
				}
			}
			
			tempValue=Integer.parseInt(tempString,16);//è½¬æ¢ä¸ºINTç±»åž‹
		}else if(returnValueType=="ASCII"){//ä½¿ç”¨tempStringè¾“å‡ºå¯¹åº”äºŒè¿›åˆ¶å?å¾—ASCç ?ä»»æ„é•¿åº¦)
			try {
				binaryProcess(mapOffset,stringLength);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("£¡£¡	Reading binary code failed, please check if the DEX file is valid.");
				e.printStackTrace();
			}	
		}else if(returnValueType=="VALUE"){//ä½¿ç”¨tempValueè¾“å‡ºå¯¹åº”äºŒè¿›åˆ¶å?å¾—INTå€?é•¿åº¦1ã€?æˆ?)
			try {
				binaryProcess(mapOffset,stringLength);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("£¡£¡	Reading binary code failed, please check if the DEX file is valid.");
				e.printStackTrace();
			}
		}
		
		
	}
	/**
	 * Read binary code from the DEX file loaded in memory 
	 * and store the result in the String-type filed 'tempString' and  the long-type filed 'tempValue'.
	 * @param The offset of the location where needs to be read
	 * @param The length of the binary code needs to be read
	 * */
	private void binaryProcess(long mapOffset,long stringLength)throws Exception{
		
			long value = 0;	
			String StringName=""; 
			if (stringLength != 0){
					if (stringLength == 1){
						value = mbb.get((int) mapOffset);					
						tempValue = value&0x000000ff;				
						tempString = Integer.toHexString((int) tempValue);
						tempString = HexToASCII(tempString);
					}else if (stringLength == 2){
						value = mbb.getShort((int) mapOffset);		
						tempValue = value&0x0000ffff;
						tempString = Integer.toHexString((int) tempValue);
						tempString = HexToASCII(tempString);
					}else if (stringLength == 4){
						value = mbb.getInt((int) mapOffset);					
						tempValue = value & 0xffffffffl;						
						tempString = Integer.toHexString((int) tempValue);
						tempString = HexToASCII(tempString);
					}else{ 
						for(int i=0;i<stringLength;i++){
							value =	mbb.get((int)(mapOffset+i))&0x000000ff;
							
							StringName = StringName+Integer.toHexString((int) value);							
						}
						StringName =HexToASCII(StringName);
						tempString = StringName;
					}
			}

		
	}
	private String HexToASCII(String hex){

		  StringBuilder sb = new StringBuilder();
		  StringBuilder temp = new StringBuilder();

		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=2 ){

		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		      sb.append((char)decimal);

		      temp.append(decimal);
		  }

		  return sb.toString();
	}	
	private String highToLow(long tempValue2){
	    String hex = "";	   
	    if(tempValue2 == 0){
	    	hex="0";
	    	return hex;
	    }
	    while(tempValue2 != 0 && tempValue2!=-1) {
	    	
	    	
	        String h = Integer.toString((int)(tempValue2 & 0xff), 16);
	        
	        
	        if((h.length() & 0x01) == 1){
	            h = '0' + h;
	        }
	        hex = hex + h;

	        tempValue2 = tempValue2 >> 8; 
					
	    }
	    
	   

	    return hex;
	}

	
}
