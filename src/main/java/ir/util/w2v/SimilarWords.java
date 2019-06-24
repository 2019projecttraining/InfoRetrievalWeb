package ir.util.w2v;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import ir.config.Configuration;
import ir.models.WordEntry;
/**
 * 
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
	
	private final static String path1=Configuration.getConfig("C:\\Users\\HPuser\\Desktop\\分词结果\\javaVector.bin");
	private final static String path2=Configuration.getConfig("C:\\Users\\HPuser\\Desktop\\分词结果\\nounWords.txt");
	/**
	 * 加载模型
	 * @param vec
	 * @return
	 * @throws IOException
	 */
	public Word2VEC getModel(Word2VEC vec) throws IOException {
		long start = System.currentTimeMillis();  
		vec.loadJavaModel(path1);
		System.out.println("load model time " + (System.currentTimeMillis() - start));
		return vec;
	}
	/**
	 * 加载名词表
	 * @return
	 * @throws IOException
	 */
	public Set<String> getNounWords() throws IOException {
		long start = System.currentTimeMillis();  
		BufferedReader br = new BufferedReader(new InputStreamReader(
		        new FileInputStream(new File(path2))));
		String temp=null;
		Set<String> nounWords=new TreeSet<>();
		while((temp=br.readLine())!=null) {
			 nounWords.add(temp);
		 }
		System.out.println("load noun time " + (System.currentTimeMillis() - start));
		return nounWords;
	}
	/**
	 * 根据阈值获取近义词
	 * @param word
	 * @param limit
	 * @param vec
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarWordsByLimit(String word,double limit,Word2VEC vec) throws IOException{//vec需提前加载模型
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
	 * TopN个近义词
	 * @param word
	 * @param n
	 * @param vec
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarWordsByTopN(String word,int n,Word2VEC vec) throws IOException{
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
	 * @param nounWords
	 * @param vec
	 * @return
	 * @throws IOException
	 */
	public Set<WordEntry> getSimilarNounWords(String word,double limit,Set<String> nounWords,Word2VEC vec) throws IOException{ 
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
	
//	public static void main(String[] args) throws IOException {
//		SimilarWords s=new SimilarWords();
//		//s.train("C:\\Users\\HPuser\\Desktop\\分词结果\\patent_fenci.txt");
//		Word2VEC wv=new Word2VEC();
//		long start = System.currentTimeMillis();
//		wv=s.getModel(wv);
//		System.out.println(s.getSimilarNounWords("显示屏", 0.1,s.getNounWords(),wv));
//	    System.out.println("use time " + (System.currentTimeMillis() - start));
//	}
}
