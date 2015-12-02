package dataComponent;

import java.util.ArrayList;
/**
 * The component contains the list of the Method Elements in DEX file
 * */
public class MethodComponent extends DataComponent{
	public ArrayList <MethodElement> methodElementList= new ArrayList <MethodElement>();
	
	public void searchingQuery(String keyword){
		for(int i=0;i<methodElementList.size();i++){
			
			if(methodElementList.get(i).methodName==keyword){
				

				System.out.println(i+" "+methodElementList.get(i));
				
			}
			
		}		
	}
}
