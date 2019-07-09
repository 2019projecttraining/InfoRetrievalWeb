package ir.util.w2v;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import ir.config.Configuration;
/**
 * 通过哈希表获取某个词的top10近义词
 * @author 杨涛
 *
 */
@Service
public class WordHashMap {
	private final static String NEAREST_WORD_CONFIG_KEY="nearest-word-path";
	private static final String path=Configuration.getConfig(NEAREST_WORD_CONFIG_KEY);
	private static final Map<String,List<WordEntry>> whm;
	static {
		whm=getHashMap();
	}
	
	private static Map<String,List<WordEntry>> getHashMap(){
		Map<String,List<WordEntry>> m=new HashMap<String,List<WordEntry>>();
		FileInputStream in;
		try {
			long start = System.currentTimeMillis();
			in = new FileInputStream(path);
			BufferedReader bf=new BufferedReader(new InputStreamReader(in,"GBK"));
			String temp=null;
			while((temp=bf.readLine())!=null) {
				List<WordEntry> l=new ArrayList<WordEntry>();
				String[] s1=temp.split(" ");
				for(String s:s1) {
					Pattern pattern = Pattern.compile("^(.*?)=");// 匹配的模式
					Matcher ma = pattern.matcher(s);
					Pattern pattern2 = Pattern.compile("=(.*?),");// 匹配的模式
					Matcher ma2 = pattern2.matcher(s);
					if(ma.find()&&ma2.find()) {
						try {
							WordEntry w=new WordEntry(ma.group(1),Float.parseFloat(ma2.group(1)));
							l.add(w);
						}catch(Exception e){}
					}
				}
				m.put(s1[0], l);
			}
			bf.close();
			System.out.println("load nearestWords hashMap time " + (System.currentTimeMillis() - start));
			return m;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public List<WordEntry> getNearWord(String word){
		long start = System.currentTimeMillis();
		List<WordEntry> l= whm.get(word);
		if(l==null)
			return Collections.emptyList();
		System.out.println("get nearest word time " + (System.currentTimeMillis() - start));
		return l;
	}
//	public static void main(String[] args) {
//		wordHashMap w=new wordHashMap();
//		System.out.println(w.getNearWord("图谱"));
//	}
}
