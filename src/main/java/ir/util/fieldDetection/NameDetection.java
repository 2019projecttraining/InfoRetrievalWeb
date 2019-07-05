package ir.util.fieldDetection;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import ir.config.Configuration;

public class NameDetection {

	private final static Set<String> names;
	
	private final static boolean enable;
	
	static {
		
		Set<String> tempSet=new HashSet<>();
		boolean tempFlag=false;
		
		try {
	
			String nameDictFilePath=Configuration.getConfig("wrong-word-analyzer-default-dict-file-path");
			
			for(String each:nameDictFilePath.split(Pattern.quote(";"))) {
				String[] sp=each.split("=");
				if(sp[0].equals("name")) {
					tempFlag=true;
					File f=new File(sp[1]);
					Scanner scan=new Scanner(new FileInputStream(f),"utf-8");
					
					while(scan.hasNextLine()) {
						String line=scan.nextLine();
						String[] sp2=line.split(" ");
						tempSet.add(sp2[0]);
					}
					scan.close();
				}
			}
			
		}catch (Exception e) {
			tempFlag=false;
			tempSet=null;
		}
		enable=tempFlag;
		names=tempSet;
	}
	
	public static boolean isName(String word) {
		return !enable?true:names.contains(word);
	}
	
}
