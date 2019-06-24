package ir.luceneIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.naming.ConfigurationException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import ir.config.Configuration;

/**
 * 读取lucene索引文件，提供接下来的查询服务
 * 
 * @author HPuser
 */
@Service
public class LuceneSearcher extends IndexSearcher{
	
	private final static String CONFIG_KEY="lucene-index-file-path";
	
	public final static String LUCENE_INDEX_FILE_PATH=Configuration.getConfig(CONFIG_KEY);
	
	static {
		if(LUCENE_INDEX_FILE_PATH==null) {
			System.err.println("警告！lucene索引位置未配置");
			throw new RuntimeException();
		}
		//指定索引库存放路径
		//E:\Lucene_index
		Directory directory;
		try {
			directory = FSDirectory.open(Paths.get(new File(LUCENE_INDEX_FILE_PATH).getPath()));
			//创建indexReader对象
			indexReader = DirectoryReader.open(directory);
			//创建indexSearcher对象
		} catch (IOException e) {
			throw new RuntimeException("Lucene 索引读取失败");
		}
	}
	
	private final static IndexReader indexReader;

	public LuceneSearcher() throws IOException{
		super(indexReader);
	}
	
}
