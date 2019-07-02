package ir.util.seg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ir.config.Configuration;

/**
 * 加载停用词字典
 * 
 * 通过配置stopwords-file-path来设置停用词的字典的位置
 * 
 * @author 余定邦
 */
public class StopWordsLoader {

	/**
	 * 配置全局的停用词
	 */
	public final static Set<String> stopWords;
	
	private final static String CONFIG_KEY="stopwords-file-path";
	
	public final static String STOPWORDS_FILE_PATH=Configuration.getConfig(CONFIG_KEY);

	static {
		stopWords=loadStopWords();
	}
	
	private final static Set<String> loadStopWords() {
		try {
			if(STOPWORDS_FILE_PATH==null) {
				System.err.println("警告！没有配置停用词词典位置，放弃使用停用词");
				return Collections.emptySet();
			}
			
			BufferedReader bfr=new BufferedReader(new FileReader(STOPWORDS_FILE_PATH));
			
			Set<String> temp=new HashSet<>();
			
			bfr.lines().forEach(t->{
				temp.add(t);
			});
			
			bfr.close();
			
			return Collections.unmodifiableSet(temp);
		}catch (Exception e) {
			System.err.println("警告！停用词加载失败，放弃使用停用词");
			return Collections.emptySet();
		}
	}
}
