package ir.util.w2v;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import ir.config.Configuration;
/**
 * 获取某个词的近义词们
 * @author 杨涛
 *
 */
@Service
public class SimilarWords {
	
//	private void train(String path) throws IOException {
//		Learn learn = new Learn();
//	    long start = System.currentTimeMillis();
//	    learn.learnFile(new File(path));
//	    System.out.println("use time " + (System.currentTimeMillis() - start));
//	    learn.saveModel(new File("C:\\Users\\HPuser\\Desktop\\分词结果\\javaVector.bin"));
//	}
	
	private final static String path1=Configuration.getConfig("similar-words-model-path");
	private final static String path2=Configuration.getConfig("noun-words-file-path");
	private final static Word2VEC vec;
	private final static Set<String> nounWords;
	
	static {
		
		vec=getModel();
		nounWords=getNounWords();
	}
	
	/**
	 * 加载模型
	 * @param vec
	 * @return
	 * @throws IOException
	 */
	private static Word2VEC getModel(){
		Word2VEC vec=new Word2VEC();
		long start = System.currentTimeMillis();  
		try {
			vec.loadJavaModel(path1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("加载近义词模型失败!");
		}
		System.out.println("load model time " + (System.currentTimeMillis() - start));
		return vec;
	}
	/**
	 * 加载名词表
	 * @return
	 * @throws IOException
	 */
	private static Set<String> getNounWords(){
		long start = System.currentTimeMillis();  
		BufferedReader br;
		String temp=null;
		Set<String> nounWords=new TreeSet<>();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path2))));
			try {
				while((temp=br.readLine())!=null) {
					 nounWords.add(temp);
				}
				br.close();
			} catch (IOException e) {
				System.err.println("加载名词表失败!");
			}
		} catch (FileNotFoundException e) {
			System.err.println("加载名词表失败!");
		}
		
		System.out.println("load noun time " + (System.currentTimeMillis() - start));
		return nounWords;
	}
	
	/**
	 * 根据相似性阈值获取近义词
	 * @param word
	 * @param limit
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarWordsByLimit(String word,double limit) throws IOException{
		 Set<WordEntry> S=vec.distance(word);
		 Set<WordEntry> S2=new TreeSet<WordEntry>();
		 for(WordEntry w: S) {
			 if(w.score>=limit) 
				 S2.add(w);
			 else break;
		 }
		 return S2;
	}
	/**
	 * 获取TopN个近义词
	 * @param word
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarWordsByTopN(String word,int n) throws IOException{
		 Set<WordEntry> S=vec.distance(word);
		 Set<WordEntry> S2=new TreeSet<WordEntry>();
		 int count=0;
		 for(WordEntry w: S) {
			 if(count<n)
				 S2.add(w);
		 }
		 return S2;
	}
	/**
	 * 根据阈值获取名词近义词
	 * @param word
	 * @param limit
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarNounWordsByLimit(String word,double limit) throws IOException{ 
		 Set<WordEntry> S=vec.distance(word);
		 Set<WordEntry> S2=new TreeSet<WordEntry>();
		 for(WordEntry w: S) {
			 if(w.score>=limit) {
				 for(String noun:nounWords) {
					 if(noun.equals(w.name)) {
						 S2.add(w);
						 S.remove(w);
						 break;
					 }
				 }
			 }
			 else break;
		 }
		 return S2;
	}
	/**
	 * 获取TopN个名词近义词
	 * @param word
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarNounWordsByTopN(String word,int n) throws IOException{
		Set<WordEntry> S=getSimilarNounWordsByLimit(word,0);
		System.out.println();
		List<WordEntry> l=new ArrayList<>(S);
		if(n<l.size())
			l=l.subList(0, n);
		Set<WordEntry> S2=new TreeSet<WordEntry>(l);
		return S2;
	}
	
	public static void main(String[] args) throws IOException {
		SimilarWords s=new SimilarWords();
		//s.train("C:\\Users\\HPuser\\Desktop\\patent_fenci.txt");
		long start = System.currentTimeMillis();
		System.out.println(s.getSimilarNounWordsByTopN("显示屏", 1));
	    System.out.println("use time " + (System.currentTimeMillis() - start));
	}
}
