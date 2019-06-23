package ir.luceneIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

@Service
public class LuceneSearcher extends IndexSearcher{
	
	public final static String LUCENE_INDEX_FILE_PATH="TODO";
	
	static {
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
