package dataComponent;

import java.util.ArrayList;
/**
 * The component contains a list of String Elements
 * */
public class StringComponent extends DataComponent{
	public ArrayList<StringElement> stringElementList = new ArrayList<StringElement>();

	public void searchingQuery(String keyword){
		for(int i=0;i<stringElementList.size();i++){
			
			if(stringElementList.get(i).stringContent==keyword){
				

			System.out.println(i+" "+stringElementList.get(i));
				
			}
			
		}		
	}

}
