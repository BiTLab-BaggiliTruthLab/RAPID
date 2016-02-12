package com.unh.unhcfreg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import systemComponent.ZipDecompressor;

public class XmlParser {
	private String xmlFile;                                                                                   
	private ZipDecompressor unpacker = new ZipDecompressor();                                                                                                                                                                       
	private String XMLPrinter2cmd;
	private	String unzippedFileDir;
	private String searchLableName=null;
	private String searchAttrName=null;
	private ArrayList<String> contentList = new ArrayList<String> ();
	
	public XmlParser(String apkFileName, String unzippedFileDir, String XMLPrinter2Path) throws Exception {
		unpacker.setApkDir(apkFileName, unzippedFileDir);
		this.unzippedFileDir=unzippedFileDir+"/AndroidManifest.txt";

			this.xmlFile=unpacker.unzipFile("AndroidManifest.xml");
			XMLPrinter2cmd="java -jar "+XMLPrinter2Path+" "+unzippedFileDir+"AndroidManifest.xml";
			final Process process = Runtime.getRuntime().exec(XMLPrinter2cmd);
		    printMessage(process.getInputStream(),this.unzippedFileDir);
		   
		    process.waitFor();
			
		
		parserXml();
		
	}
	public void searchAttr(String lableName,String attributeName){
		this.searchLableName=lableName;
		this.searchAttrName=attributeName;
		
	}
	private static void printMessage(final InputStream input,final String unzippedFileDir) {
	     new Thread(new Runnable() {

			@Override
	        public void run() {

			         Reader reader = new InputStreamReader(input);
			         BufferedReader bf = new BufferedReader(reader);
			         String line;
			         
			         try {
			       
			        	FileWriter writer = new FileWriter(unzippedFileDir); 
						BufferedWriter bw = new BufferedWriter(writer);
						while((line=bf.readLine())!=null) {
						   
						     bw.write(line+"\r\n");
						 }
						bf.close();
			            
						bw.close();
						reader.close();
						writer.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
			          
			           
			         
			}

         }).start();
	}
	public ArrayList<String> parserXml() throws Exception{ 

		
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		DocumentBuilder db = dbf.newDocumentBuilder();

			
			Document doc = db.parse(this.unzippedFileDir);  
			Element root =doc.getDocumentElement();
			NodeList nodeList = root.getChildNodes();
			if(searchLableName!=null && searchAttrName!=null){
				nodeTraverse(nodeList);
			}

		 
		
		
		
		return contentList;


	} 
	private NodeList nodeTraverse(NodeList nodeList){
		NodeList n2=null;
		for (int i = 0; i < nodeList.getLength(); i++) {
            	 Node child = nodeList.item(i);
            	 if (child.getNodeType() == Node.ELEMENT_NODE){
            		 
            		 if(child.getNodeName().equals(searchLableName)){
            			  
            			 NamedNodeMap attributes= child.getAttributes();
            		
            			 for(int j=0; j< attributes.getLength(); j++ ){
            			
            				 Attr attr = (Attr) attributes.item(j);
            				
            				 if(attr.getName().equals(this.searchAttrName)){
            					
            					 this.contentList.add(attr.getValue());
            					 
            				 }
            			
            			 }
            		 
            		 }
            		 
            	 }
            	 
            	 nodeTraverse(child.getChildNodes());
        }
		return n2;
	}

}
