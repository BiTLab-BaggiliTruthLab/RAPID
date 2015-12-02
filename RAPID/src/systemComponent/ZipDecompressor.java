package systemComponent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
/**class for unzipping DEX file from APK file*/
public class ZipDecompressor {
	
	private String apkDir;	
	private String unzippedDir;
	
	public ZipDecompressor(){
		
	}
	/**setting the full path for either storing APK files and 
	 * the directory of storing the unzipped DEX file if its necessary for the users
	 * @param apkDir full path storing APK files
	 * @param unzippedDir full path with a directory storing the unzipped DEX file
	 * */	
	public void setApkDir(String apkDir, String unzippedDir){
		this.apkDir=apkDir;
		this.unzippedDir=unzippedDir;
	}
	/**unzipping a file*/

	public String unzipFile(String unzippingFileName) throws Exception{

		
		
		//System.out.println(apkDir); 
		FileInputStream fileInput=new FileInputStream(new File(apkDir));
		ZipInputStream zin=new ZipInputStream(fileInput);
        
       
	            	BufferedInputStream bin=new BufferedInputStream(zin);              
	            	if(unzippedDir!=null){
	            		File uzDir=new File (unzippedDir);
	            		if(!uzDir.exists()){
	            			uzDir.mkdir();  
	            		}
	            	}
	            	
		            	
	            	String parent=unzippedDir; 
		            ZipEntry entry;  
		                while((entry = zin.getNextEntry())!= null){  		                	
		                	 //System.out.println(entry.getName()); 
		                    if(entry.getName().equals(unzippingFileName)&& (!entry.isDirectory())){  
		                    
		                    	parent = parent+entry.getName();
			                    FileOutputStream out=new FileOutputStream(parent);  
			                    BufferedOutputStream Bout=new BufferedOutputStream(out);  
			                    int b;  
				                while((b=bin.read())!=-1){  
				                        Bout.write(b);  
				                }  
				                Bout.close();  
				                out.close();  
				                break;
		                    }   
		                }		               
		                bin.close();  
		                zin.close();  
		                fileInput.close();

		               //fc.close();		               
		                return parent;
					
		            	
		
	}
	/**read the content of a file*/
	 public void readZipFile(String file) throws Exception {  
         ZipFile zf = new ZipFile(this.apkDir);  
         InputStream in = new BufferedInputStream(new FileInputStream(apkDir));  
         ZipInputStream zin = new ZipInputStream(in);  
         ZipEntry ze;  
         while ((ze = zin.getNextEntry()) != null) {  
             if (ze.isDirectory()) {
             } else {  
                 System.err.println("file - " + ze.getName() + " : "  
                         + ze.getSize() + " bytes");  
                 long size = ze.getSize();  
                 if(ze.getName().equals(file)){ 
	                 
	                     BufferedReader br = new BufferedReader(  
	                             new InputStreamReader(zf.getInputStream(ze)));  
	                     String line;  
	                     while ((line = br.readLine()) != null) {  
	                         System.out.println(line);  
	                     }  
	                     br.close();  
	                  
                 }
                 System.out.println();  
             }  
         }  
         zin.closeEntry();  
     }  
	
}
