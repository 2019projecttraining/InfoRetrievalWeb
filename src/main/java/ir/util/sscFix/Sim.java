package ir.util.sscFix;
//package ir.util.ssc_fix;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//
//import ir.config.Configuration;
//
//
///**
// * 使用音形码来对用户搜索的词进行修正
// * 
// * @author 余定邦
// *
// */
//public class Sim {
//	
//	public static Map<Character,String> yinxinCodeDict;
//	
//	private static Map<Integer,Map<String,Integer>> wordDict;
//	
//	private static Map<Integer,Map<String,Integer>> nameDict;
//	
//	private final static String yinxinCodeDictPath=Configuration.getConfig("ssc-code-file-path");//"C:\\Users\\HPuser\\Desktop\\hanzi_ssc_fix.txt"
//	
//	private final static String wordDictPath=Configuration.getConfig("word-dict-path");//"C:\\Users\\HPuser\\Desktop\\worddict.txt"
//	
//	private final static String nameDictPath=Configuration.getConfig("name-dict-path");//"C:\\Users\\HPuser\\Desktop\\namedict.txt"
//	
//	public final static boolean ENABLE_WORD_CHECK="TRUE".equals(Configuration.getConfig("enable-word-check").toUpperCase());
//	
//	public final static boolean ENABLE_NAME_CHECK="TRUE".equals(Configuration.getConfig("enable-name-check").toUpperCase());
//	
//	static {
//		long a=System.currentTimeMillis();
//		
//		if(ENABLE_WORD_CHECK||ENABLE_NAME_CHECK)
//			loadYinXinCodeDict(yinxinCodeDictPath);
//		
//		wordDict=new HashMap<>(20);
//		
//		if(ENABLE_WORD_CHECK)
//			loadDicts(wordDictPath,wordDict);
//		
//		nameDict=new HashMap<>(20);
//		
//		if(ENABLE_NAME_CHECK)
//			loadDicts(nameDictPath,nameDict);
//		
//		System.out.println("time："+(System.currentTimeMillis()-a));
//		
//		System.out.println("加载完成");
//	}
//	
//	/**
//	 * 加载音形码字典
//	 * 
//	 * @param path 字典位于磁盘路径
//	 */
//	private static void loadYinXinCodeDict(String path) {
//		yinxinCodeDict=new HashMap<>(90000);
//		
//		File f=new File(path);
//		Scanner scan;
//		try {
//			scan = new Scanner(new FileInputStream(f),"utf-8");
//			while(scan.hasNextLine()) {
//				String line=scan.nextLine();
//				String[] sp=line.split(" ");
//				yinxinCodeDict.put(sp[0].charAt(0), sp[1]);
//			}
//			scan.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 加载词库字典
//	 * 
//	 * @param path 字典位于磁盘路径
//	 */
//	private static void loadDicts(String path,Map<Integer,Map<String,Integer>> map) {
//		File f=new File(path);
//		Scanner scan;
//		try {
//			scan = new Scanner(new FileInputStream(f),"utf-8");
//			while(scan.hasNextLine()) {
//				String line=scan.nextLine();
//				String[] sp=line.split(" ");
//				if(!map.containsKey(sp[0].length()))
//					if(sp[0].length()>=2&&sp[0].length()<=4)
//						map.put(sp[0].length(),new HashMap<>(10000));
//					else
//						map.put(sp[0].length(),new HashMap<>(100));
//				map.get(sp[0].length()).put(sp[0],Integer.parseInt(sp[1]));
//			}
//			scan.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 获取两个字的相似程度
//	 * 
//	 * @param character1
//	 * @param character2
//	 * @return character1和character2的相似程度
//	 */
//	public static double yinxinCodeSim(char character1,char character2) {
//		String code1=yinxinCodeDict.get(character1);
//		String code2=yinxinCodeDict.get(character2);
//		
//		if(code1==null||code2==null)
//			return 0;
//		
//		//字音
//		int yunmu=difChar(code1,code2,0);//声母
//		int shengmu=difChar(code1,code2,1);//韵母
//		int yunmubuma=difChar(code1,code2,2);//韵母补码
//		int shengdiao=difChar(code1,code2,3);//声调
//		
//		//字形
//		int jiegou=difChar(code1,code2,4);//结构
//		
//		//四角编码
//		int sijiao1=difChar(code1,code2,5);
//		int sijiao2=difChar(code1,code2,6);
//		int sijiao3=difChar(code1,code2,7);
//		int sijiao4=difChar(code1,code2,8);
//		//int sijiao5=difChar(code1,code2,9);//这个位置和字的形状没有关系，因此不使用
//		
//		//笔画数
//		int bihua1=getbihua(code1);
//		int bihua2=getbihua(code2);
//		
//		/**
//		 * 字音中声母和韵母各占37.5%，辅韵母和声调各占12.5%
//		 * 
//		 * 字形中结构占30%，四角编码占40%，笔画占30%
//		 */
//		
//		double ziyinSim=yunmu*0.375+shengmu*0.375+yunmubuma*0.125+shengdiao*0.125;
//		double zixinSim=jiegou*0.3+0.1*(sijiao1+sijiao2+sijiao3+sijiao4)+(1-(Math.abs(bihua1-bihua2)*1.0/Math.max(bihua1, bihua2)))*0.3;
//		
//		return Math.max(ziyinSim, zixinSim);
//	}
//	
//	/**
//	 * 获取音形码中的笔画
//	 * 
//	 * @param code
//	 * @return
//	 */
//	private static int getbihua(String code) {
//		Character c=code.charAt(10);
//		
//		if(c>='0'&&c<='9')
//			return c-'0';
//		else
//			return c-'A'+10;
//	}
//	
//	/**
//	 * 音形码的各位比较
//	 * 
//	 * @param str1
//	 * @param str2
//	 * @param index
//	 * @return
//	 */
//	private static int difChar(String str1,String str2,int index) {
//		return str1.charAt(index)==str2.charAt(index)?1:0;
//	}
//	
//	/**
//	 * 获取两个词的相似度
//	 * 
//	 * 相似度评价是将所有字的相似度相加除以字的总数
//	 * 
//	 * @param str1
//	 * @param str2
//	 * @return
//	 */
//	public static double getSim(String str1,String str2) {
//		
//		if(str1.length()!=str2.length())
//			return 0;
//		
//		double sim=0;
//		
//		int len=str1.length();
//		
//		for(int i=0;i<len;++i) {
//			double point;
//			if(str1.charAt(i)==str2.charAt(i)) 
//				point=1;
//			else {
//				point=yinxinCodeSim(str1.charAt(i), str2.charAt(i));
//			}
//			
//			sim+=point/len;
//		}
//		
//		return sim;
//	}
//	
//	public static void getNearWords(String word,Map<String,Integer> dict,Map<String,Double> collector,double threshold){
//		
//		for(String dictWord:dict.keySet()) {
//			if(dictWord.equals(word)&&dict.get(dictWord)>1)
//				collector.put(dictWord,10.0);
//			else {
//				double sim=getSim(word,dictWord);
//				if(sim<threshold)
//					continue;
//				double sim2=sim*(1+(Math.log10(dict.get(dictWord)+1)/10));
//				if(sim2>=threshold*1.25)
//					collector.put(dictWord,sim2);
//			}
//		}
//	}
//	
//	/**
//	 * 获取最可能的一些词，如果词典中出现查找的这个词，那么总是认为这个词是最可能性最高的
//	 * 
//	 * @param word
//	 * @param threshold
//	 * @return
//	 */
//	public static List<String> getNearWords(String word,double threshold){
//		if((!ENABLE_NAME_CHECK)&&(!ENABLE_WORD_CHECK))
//			return Collections.emptyList();
//		
//		Map<String,Double> map=new HashMap<>();
//		
//		if(ENABLE_NAME_CHECK)
//			getNearWords(word, nameDict.get(word.length()), map, threshold);
//		
//		if(ENABLE_WORD_CHECK)
//			getNearWords(word, wordDict.get(word.length()), map, threshold);
//		
//		//System.out.println(map);
//		
//        List<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(
//        		map.entrySet());
//        Collections.sort(entryList, (a,b)->b.getValue().compareTo(a.getValue()));
//		
//        List<String> nearWord=new ArrayList<>();
//        for(Map.Entry<String, Double> entry:entryList) 
//        	nearWord.add(entry.getKey());
//        return nearWord;
//	}
//	
//	/**
//	 * 获取最可能的一个词
//	 * 
//	 * @param word
//	 * @param threshold
//	 * @return
//	 */
//	public static String nearestWord(String word,double threshold) {
//		List<String> words=getNearWords(word,threshold);
//		if(words.size()==0)
//			return word;
//		else
//			return words.get(0);
//	}
//	
//	public static void main(String[] args) throws IOException {
//		Scanner scan=new Scanner(System.in);
//		
//		while(scan.hasNext()) {
//			long a=System.currentTimeMillis();
//			System.out.println(nearestWord(scan.nextLine(),0.80));
//			System.out.println("time："+(System.currentTimeMillis()-a));
//		}
//		
//		scan.close();
////		long a=System.currentTimeMillis();
////		System.out.println(getCharacterSim('计','肌'));
////		System.out.println("time："+(System.currentTimeMillis()-a));
////		a=System.currentTimeMillis();
////		System.out.println(yinxinCodeSim('计','肌'));
////		System.out.println("time："+(System.currentTimeMillis()-a));
//	}
//	
//}
