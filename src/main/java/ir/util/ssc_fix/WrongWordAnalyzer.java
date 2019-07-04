package ir.util.ssc_fix;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import ir.config.Configuration;

public class WrongWordAnalyzer {
	
	public final static WrongWordAnalyzer DEFAULT_WRONG_WORD_ANALYZER=new WrongWordAnalyzer(
			Configuration.getConfig("wrong-word-analyzer-default-field"),
			Configuration.getConfig("wrong-word-analyzer-default-dict-file-path"),
			Configuration.getConfig("wrong-word-analyzer-default-enabled-field"));

	/**
	 * 略微复杂数据结构，有多层的HashMap，为了提高速度，并且划分字典区域
	 * 
	 * Map<String,Map<Integer,Map<String,Integer>>> 对应的是域和这个域内的词典
	 * 
	 * Map<Integer,Map<String,Integer>> 对应的是词长度和这个词长度的所有词
	 * 
	 * Map<String,Integer> 对应的是词和词频
	 */
	private Map<String,Map<Integer,Map<String,Integer>>> fieldWordDictMap;
	
	/**
	 * 设置哪些域需要使用
	 * 
	 */
	private Map<String,Boolean> fieldEnable;
	
	/**
	 * 所有的域
	 */
	private Set<String> fields;
	
	/**
	 * 初始化，使用三个配置项进行配置
	 * 
	 * @param fieldConfig		所有的域
	 * @param dictPathConfig	所有的域对应的词典
	 * @param fieldEnableConfig	那些域需要进行检查
	 */
	public WrongWordAnalyzer(String fieldConfig, String dictPathConfig, String fieldEnableConfig){
		
		if(fieldConfig==null||dictPathConfig==null||fieldEnableConfig==null) {
			System.err.println("错误的初始化参数 ： class WrongWordAnalyzer");
			throw new RuntimeException();
		}
		
		//读取配置中所有的域
		String[] fields=fieldConfig.split(Pattern.quote(";"));
		
		this.fields=new HashSet<>(fields.length);
		
		for(String field:fields)
			this.fields.add(field);
		
		fieldEnable=new HashMap<>(fields.length);
		
		//设置这些域是否需要使用
		String[] enables=fieldEnableConfig.split(Pattern.quote(";"));
		
		for(String enable:enables) {
			if(this.fields.contains(enable)) {
				fieldEnable.put(enable, true);
			}
		}
		
		for(String field:this.fields) {
			if(!fieldEnable.containsKey(field))
				fieldEnable.put(field, false);
		}
		
		//对于需要使用的域，去读取这个域的词典
		fieldWordDictMap=new HashMap<>(fields.length);
		
		String[] paths=dictPathConfig.split(Pattern.quote(";"));
		
		for(String path:paths) {
			String[] fieldAndPath=path.split(Pattern.quote("="));
			
			if(fieldAndPath.length!=2) {
				System.err.println("错字分词器配置加载错误！");
				throw new RuntimeException();
			}
			
			String fieldName=fieldAndPath[0];
			String filepath=fieldAndPath[1];
			
			if(fieldEnable.getOrDefault(fieldName, false)){
				Map<Integer,Map<String,Integer>> dict=new HashMap<>(20);
				loadDicts(filepath,dict);
				fieldWordDictMap.put(fieldName, dict);
			}
		}
		
		Ssc_Similarity.init();
	}
	
	/**
	 * 加载词库字典
	 * 
	 * @param path 字典位于磁盘路径
	 */
	private static void loadDicts(String path,Map<Integer,Map<String,Integer>> map) {
		File f=new File(path);
		Scanner scan;
		try {
			scan = new Scanner(new FileInputStream(f),"utf-8");
			while(scan.hasNextLine()) {
				String line=scan.nextLine();
				String[] sp=line.split(" ");
				if(!map.containsKey(sp[0].length()))
					if(sp[0].length()>=2&&sp[0].length()<=4)
						map.put(sp[0].length(),new HashMap<>(10000));
					else
						map.put(sp[0].length(),new HashMap<>(100));
				map.get(sp[0].length()).put(sp[0],Integer.parseInt(sp[1]));
			}
			scan.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取最可能的一些词，如果词典中出现查找的这个词，那么总是认为这个词是最可能性最高的
	 * 
	 * @param word
	 * @param threshold		阈值，超过这个阈值相似度的词才被返回
	 */
	public List<String> getSimilarWord(String word,double threshold,String... fields) {
		
		int len=word.length();
		
		Map<String,Double> collector=new HashMap<>();
		
		for(String field:fields) {
			if(fieldEnable.getOrDefault(field, false)) {
				
				Map<Integer,Map<String,Integer>> wordMapInField=fieldWordDictMap.get(field);
				
				if(wordMapInField==null)
					continue;
				
				Map<String,Integer> dict=wordMapInField.get(len);
				
				for(String dictWord:dict.keySet()) {
					if(dictWord.equals(word)&&dict.get(dictWord)>1)
						collector.put(dictWord,10.0);
					else {
						double sim=Ssc_Similarity.getSimilarity(word,dictWord);
						if(sim<threshold)
							continue;
						double sim2=sim*(1+(Math.log10(dict.get(dictWord)+1)/10));
						if(sim2>=threshold*1.25)
							collector.put(dictWord,sim2);
					}
				}
				
			}
		}
		
		List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(
    		  collector.entrySet());
		Collections.sort(entryList, (a,b)->b.getValue().compareTo(a.getValue()));
		
		List<String> nearWord=new ArrayList<>();
		for(Map.Entry<String, Double> entry:entryList) 
			nearWord.add(entry.getKey());
		
		return nearWord;
	}
	
	/**
	 * 获取最可能的一个词，如果词库中没有，那么返回查询的word本身
	 * 
	 * @param word
	 * @param threshold		阈值，超过这个阈值相似度的词才被返回
	 */
	public String correctWord(String word,double threshold,String... fields) {
		List<String> words=getSimilarWord(word,threshold,fields);
		if(words.size()==0)
			return word;
		else
			return words.get(0);
	}
	
	public static void main(String[] args) {	
		Scanner scan=new Scanner(System.in);
		
		while(scan.hasNext()) {
			long a=System.currentTimeMillis();
			System.out.println(DEFAULT_WRONG_WORD_ANALYZER.correctWord(scan.next(), 0.9, "name", "word"));
			System.out.println("time："+(System.currentTimeMillis()-a));
		}
		
		scan.close();
	}
	
}
