package ir.util.fieldDetection;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import ir.config.Configuration;

/**
 * 探测器，通过加载域内固定的姓名或公司名来检测某个词应该属于哪个域
 * 
 * @author 余定邦
 *
 */
public class Detector {

	private final Set<String> words;
	
	private final boolean enable;
	
	public Detector(String field,String fieldConfigKey){
		Set<String> tempSet=new HashSet<>();
		boolean tempFlag;
		
		try {
			String nameDictFilePath=Configuration.getConfig(fieldConfigKey);
			File f=new File(nameDictFilePath);
			Scanner scan=new Scanner(new FileInputStream(f),"utf-8");
			
			while(scan.hasNextLine()) {
				String line=scan.nextLine();
				String[] sp2=line.split(" ");
				tempSet.add(sp2[0]);
			}
			scan.close();
			
			tempFlag=true;
		}catch (Exception e) {
			System.err.println(field+"域探测器加载失败！");
			tempFlag=false;
			tempSet=null;
		}
		enable=tempFlag;
		words=tempSet;
	}
	
	public boolean detect(String word) {
		return !enable?true:words.contains(word);
	}
	
}
