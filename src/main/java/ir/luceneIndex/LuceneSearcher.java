package ir.luceneIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import ir.config.Configuration;
import ir.enumDefine.SearchAccuracy;

/**
 * 读取lucene索引文件，提供接下来的查询服务
 * 
 * @author 余定邦
 */
@Service
public class LuceneSearcher{
	
	public final static Map<SearchAccuracy,IndexSearcher> indexes;
	
	private final static String COARSE_GRAINED_CONFIG_KEY="accurate-lucene-index-file-path";
	
	public final static String COARSE_GRAINED_LUCENE_INDEX_FILE_PATH=Configuration.getConfig(COARSE_GRAINED_CONFIG_KEY);
	
	private final static String FINE_GRAINED_CONFIG_KEY="fuzzy-lucene-index-file-path";
	
	public final static String FINE_GRAINED_LUCENE_INDEX_FILE_PATH=Configuration.getConfig(FINE_GRAINED_CONFIG_KEY);
	
	private final static String SINGLE_WORD_CONFIG_KEY="single-word-lucene-index-file-path";
	
	public final static String SINGLE_WORD_LUCENE_INDEX_FILE_PATH=Configuration.getConfig(SINGLE_WORD_CONFIG_KEY);
	
	private final static String DOUBLE_WORD_CONFIG_KEY="double-word-lucene-index-file-path";
	
	public final static String DOUBLE_WORD_LUCENE_INDEX_FILE_PATH=Configuration.getConfig(DOUBLE_WORD_CONFIG_KEY);
	
	static {
		Map<SearchAccuracy,IndexSearcher> temp=new HashMap<>();
		
		IndexSearcher tempIndex;
		
		System.out.println("------------");
        System.out.println("加载索引文件");
        System.out.println("------------");
		
		if(COARSE_GRAINED_LUCENE_INDEX_FILE_PATH!=null&&(tempIndex=indexLoad("粗粒度",COARSE_GRAINED_LUCENE_INDEX_FILE_PATH))!=null) 
			temp.put(SearchAccuracy.ACCURATE, tempIndex);
		
		if(FINE_GRAINED_LUCENE_INDEX_FILE_PATH!=null&&(tempIndex=indexLoad("细粒度",FINE_GRAINED_LUCENE_INDEX_FILE_PATH))!=null)
			temp.put(SearchAccuracy.FUZZY, tempIndex);
		
		if(SINGLE_WORD_LUCENE_INDEX_FILE_PATH!=null&&(tempIndex=indexLoad("单字",SINGLE_WORD_LUCENE_INDEX_FILE_PATH))!=null)
			temp.put(SearchAccuracy.SINGLE_WORD, tempIndex);
		
		if(DOUBLE_WORD_LUCENE_INDEX_FILE_PATH!=null&&(tempIndex=indexLoad("双字",DOUBLE_WORD_LUCENE_INDEX_FILE_PATH))!=null)
			temp.put(SearchAccuracy.DOUBLE_WORD, tempIndex);
		
		System.out.println();
		
		indexes=Collections.unmodifiableMap(temp);
	}
	
	private static IndexSearcher indexLoad(String indexName,String path) {
		//指定索引库存放路径
		//E:\Lucene_index
		try {
			Directory  directory = FSDirectory.open(Paths.get(new File(path).getPath()));
			//创建indexReader对象
			DirectoryReader indexReader = DirectoryReader.open(directory);
			
			IndexSearcher temp=new IndexSearcher(indexReader);
			
			System.out.println(indexName+"索引加载成功");
			
			return temp;
			//创建indexSearcher对象
		} catch (IOException e) {
			System.err.println("索引读取失败,path="+path);
			return null;
		}
	}
	
}
