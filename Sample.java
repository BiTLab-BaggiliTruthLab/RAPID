
import java.util.ArrayList;
import java.util.Map.Entry;

import com.unh.unhcfreg.RapidAndroidParser;
import com.unh.unhcfreg.QueryBlock;

import dataComponent.Instruction;
import dataComponent.MethodElement;
import dataComponent.StringElement;



public class Sample {
	public static void main(String args[]){
		
		final RapidAndroidParser rapid = new RapidAndroidParser();
		 rapid.setApkDir("C:/Users/XIAOLU/Documents/GitHub/RAPID/SampleApk/");
		
		 rapid.setQuery(new QueryBlock(){
		 
			public void queries() {
				// TODO Auto-generated method stub
				//Print general information of DEX file
				System.out.println("This DEX file has "+rapid.getStringList().size()+" Strings,"+
									rapid.getApiList().size()+" APIs/"+
									rapid.getMethodList().size()+" Methods.");
				
				//print sting list
				
				ArrayList<StringElement>stringList=rapid.getStringList();
				System.out.println("The first 20 strings of the string component:	");
				for(int i = 0; i< stringList.size(); i ++){
					if(i==20){
						break;
					}
					System.out.println("	"+stringList.get(i).stringContent);
				}
				
				
				// print API list
				ArrayList<MethodElement>apiList=rapid.getApiList();
				System.out.println("The first 20 APIs of the method component:	");
				for(int j = 0; j<apiList.size(); j++){
					if(j==20){
						break;
					}
					apiList.get(j).printFields();
				}
				
				//Create a Method object

				MethodElement targetMethod =new MethodElement("java.lang.System","load",null,null);

				 
				//Determine if the Method exists in DEX file.
				if (rapid.isMethodExist(targetMethod)){
					
					//Searching the invokes of the Method;
					ArrayList<Instruction> insList=rapid.getInvokedInstruction(targetMethod);
					if(insList!=null){
						System.out.print(insList.size()+" instructions are found involking Method:");
						targetMethod.printFields();
						for(Instruction ins:insList){
								ins.printFields();
							}
					}
					
					//Searching Methods invoking targetMethod				
					
					ArrayList<MethodElement> methodList= rapid.getMethodInvolker(targetMethod);
					if(methodList!=null){
						System.out.print(methodList.size()+" methods are found calling the method:");
						targetMethod.printFields();
						
						for(MethodElement me:methodList){					
							me.printFields();
						}	
						for(MethodElement me:methodList){					
							//print code block of the method
							me.codeBlock.printInsList();
						}
					
					}
					//Searching 
					if(rapid.getExternalFilesDirectory().entrySet()!=null){
						for(Entry<Long, String> entry:rapid.getExternalFilesDirectory().entrySet()){ 
				          System.out.println(entry.getKey()+"--->"+entry.getValue()); 
						} 
					}
					
					
					
				}	
				
			}
		
		 });
	}
}
