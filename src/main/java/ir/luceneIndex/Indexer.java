package ir.luceneIndex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.huaban.analysis.jieba.JiebaSegmenter;

import ir.config.Configuration;
import ir.enumDefine.SearchAccuracy;
import ir.util.seg.jieba.JiebaAnalyzer;
import net.sourceforge.pinyin4j.PinyinHelper;

public class Indexer {
	/**
	 * 按照不同的索引文件写入对应索引目录
	 * 
	 * @author WC
	 */
	public static void luceneCreateIndex(SearchAccuracy search) throws Exception {
		// 默认指定索引存放的位置
		String path = Configuration.getConfig("fuzzy-lucene-index-file-path");

		// System.out.println("pathname" + Paths.get(new
		// File(Configuration.getConfig("fuzzy-lucene-index-file-path")).getPath()));
		// jieba切分 注意index模式(分词粗)和search模式(相对细)
		// JiebaAnalyzer jiebaAnalyzer = new
		// JiebaAnalyzer(JiebaSegmenter.SegMode.INDEX);
		Analyzer analyzer = new JiebaAnalyzer(JiebaSegmenter.SegMode.INDEX);

		// 判断分词器选择
		switch (search) {
		case FUZZY:
			analyzer = new JiebaAnalyzer(JiebaSegmenter.SegMode.INDEX);
			path = Configuration.getConfig("fuzzy-lucene-index-file-path");
			break;
		case ACCURATE:
			analyzer = new JiebaAnalyzer(JiebaSegmenter.SegMode.SEARCH);
			path = Configuration.getConfig("accurate-lucene-index-file-path");
			break;
		case SINGLE_WORD:
			// 单字符切分
			analyzer = new StandardAnalyzer();
			path = Configuration.getConfig("single-word-lucene-index-file-path");
			break;
		case DOUBLE_WORD:
			// 双字符切分
			analyzer = new CJKAnalyzer();
			path = Configuration.getConfig("double-word-lucene-index-file-path");
			break;
		default:
			break;
		}

		// 默认fuzzy查询 jieba index模式
		Directory directory = FSDirectory.open(Paths.get(new File(path).getPath()));

		// 从下列分词器创建一个分词器
		// 单字符切分
		// StandardAnalyzer analyzer = new StandardAnalyzer();
		// 双字符切分
		// CJKAnalyzer cjkAnalyzer = new CJKAnalyzer();
		// 智能中文分词器
		// SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
		// 创建indexwriterConfig(参数分词器)
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		// 创建indexwriter对象(文件对象,索引配置对象)
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		// 读csv
		String[] headers = new String[] { "id", "abstract", "address", "applicant", "application_date",
				"application_number", "application_publish_number", "classification_number", "filing_date",
				"grant_status", "inventor", "title", "year" };
		String csvPath = "D:\\Lucene_Path\\newpatent.csv";

		FileReader reader = new FileReader(csvPath);
		CSVParser parser = CSVFormat.DEFAULT.withHeader(headers).parse(reader);

		for (CSVRecord record : parser) {

			System.out.println(record.get("title"));
			// preserved_type保护域不分词
			// default_type默认分词
			// 分词模式DOCS
			FieldType preserved_type = new FieldType();
			preserved_type.setTokenized(false);
			preserved_type.setStored(true);
			preserved_type.setIndexOptions(IndexOptions.DOCS);
			FieldType default_type = new FieldType();
			default_type.setTokenized(true);
			default_type.setStored(true);
			default_type.setIndexOptions(IndexOptions.DOCS);

			// 创建文件域名
			// Field(域的名称,域的内容,fieldtype)
			Field id_field = new Field("id", record.get("id"), preserved_type);
			Field abstract_field = new Field("abstract", record.get("abstract"), default_type);
			Field address_field = new Field("address", record.get("address"), default_type);
			Field applicant_field = new Field("applicant", record.get("applicant"), preserved_type);

			Field application_date_field = new Field("application_date", record.get("application_date"),
					preserved_type);
			Field application_number_field = new Field("application_number", record.get("application_number"),
					preserved_type);
			Field application_publish_number_field = new Field("application_publish_number",
					record.get("application_publish_number"), preserved_type);

			Field classification_number_field = new Field("classification_number", record.get("classification_number"),
					preserved_type);
			Field filing_date_field = new Field("filing_date", record.get("filing_date"), preserved_type);
			Field grant_status_field = new Field("grant_status", record.get("grant_status"), preserved_type);

			// Field inventor_field = new Field("inventor", record.get("inventor"),
			// preserved_type);
			Field title_field = new Field("title", record.get("title"), default_type);
			Field year_field = new Field("year", record.get("year"), preserved_type);

			Document indexableFields = new Document();
			indexableFields.add(id_field);
			indexableFields.add(abstract_field);
			indexableFields.add(address_field);
			indexableFields.add(applicant_field);

			indexableFields.add(application_date_field);
			indexableFields.add(application_number_field);
			indexableFields.add(application_publish_number_field);

			indexableFields.add(classification_number_field);
			indexableFields.add(filing_date_field);
			indexableFields.add(grant_status_field);

			// indexableFields.add(inventor_field);
			indexableFields.add(title_field);
			indexableFields.add(year_field);

			// 添加姓名域以及首字母域
			// 姓名域改为多值域
			String inventorS = record.get("inventor");
			String inventor[] = inventorS.split(";");
			for (String out : inventor) {
				String firstW = "";
				System.out.println(out);
				if (out.charAt(0) >= 'A' && out.charAt(0) <= 'Z' || out.charAt(0) >= 'a' && out.charAt(0) <= 'z') {
					firstW = String.valueOf(out.charAt(0));
					// System.out.println(firstW);
				} else {
					String temp[] = PinyinHelper.toHanyuPinyinStringArray(out.charAt(0));
					if (temp != null)
						firstW = String.valueOf(temp[0].charAt(0));
					System.out.println(firstW);
				}
				firstW = firstW.toUpperCase();
				// 姓名域
				Field inventor_field = new Field("inventor", out, preserved_type);
				// 首字母域FirstWorld
				Field inventor_firstW_field = new Field("inventor_firstW", firstW, preserved_type);

				indexableFields.add(inventor_field);
				indexableFields.add(inventor_firstW_field);
			}

			// indexableFields.add(fileSizeField);
			// 创建索引 并写入索引库
			indexWriter.addDocument(indexableFields);

		}
		reader.close();
		// 关闭indexWriter
		indexWriter.close();
	}

	// 多条件查询
	public static void boolean_search(String content) throws IOException {
		// 指定索引库存放路径
		// E:\Lucene_Path\Lucene_index
		Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		// 创建indexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		// 创建indexSearcher对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		/** 创建多条件查询 **/
		// BooleanQuery query = new BooleanQuery();
		// String searchField="INVENTOR";
		// String keyword=content;
		TermQuery query1 = new TermQuery(new Term("abstract", content));
		TermQuery query2 = new TermQuery(new Term("address", content));
		TermQuery query3 = new TermQuery(new Term("grant_status", content));
		TermQuery query4 = new TermQuery(new Term("inventor", content));
		TermQuery query5 = new TermQuery(new Term("title", content));
		TermQuery query6 = new TermQuery(new Term("year", content));
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		// 1．MUST和MUST：取得连个查询子句的交集。
		// 2．MUST和MUST_NOT：表示查询结果中不能包含MUST_NOT所对应得查询子句的检索结果。
		// 3．SHOULD与MUST_NOT：连用时，功能同MUST和MUST_NOT。
		// 4．SHOULD与MUST连用时，结果为MUST子句的检索结果,但是SHOULD可影响排序。
		// 5．SHOULD与SHOULD：表示“或”关系，最终检索结果为所有检索子句的并集。
		// 6．MUST_NOT和MUST_NOT：无意义，检索无结果。
		builder.add(query1, Occur.SHOULD);
		builder.add(query2, Occur.SHOULD);
		builder.add(query3, Occur.SHOULD);
		builder.add(query4, Occur.SHOULD);
		builder.add(query5, Occur.SHOULD);
		builder.add(query6, Occur.SHOULD);
		BooleanQuery booleanQuery = builder.build();

		// 普通查询 域名 查询内容
		// TermQuery query = new TermQuery(new Term("abstract", content));

		// 执行查询
		TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
		System.out.println("查询结果的总数" + topDocs.totalHits);
		// 遍历查询结果
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			// scoreDoc.doc 属性就是doucumnet对象的id
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.getField("id"));
			System.out.println(doc.getField("abstract"));
			System.out.println(doc.getField("inventor"));
			// System.out.println(doc.getField("ABSTRACT"));
			// System.out.println(doc.getField("INVENTOR"));
			/*
			 * System.out.println(doc.getField("fileName"));
			 * System.out.println(doc.getField("fileContent"));
			 * System.out.println(doc.getField("filePath"));
			 * System.out.println(doc.getField("fileSize"));
			 */
		}
		indexReader.close();
	}

	// 单域多条件查询
	public static void multi_search(String field, String[] content, SearchAccuracy search) throws IOException {
		// 指定索引库存放路径
		// 默认指定索引存放的位置
		String path = Configuration.getConfig("fuzzy-lucene-index-file-path");

		// 判断分词器路径选择
		switch (search) {
		case FUZZY:
			path = Configuration.getConfig("fuzzy-lucene-index-file-path");
			break;
		case ACCURATE:
			path = Configuration.getConfig("accurate-lucene-index-file-path");
			break;
		case SINGLE_WORD:
			// 单字符切分
			path = Configuration.getConfig("single-word-lucene-index-file-path");
			break;
		case DOUBLE_WORD:
			// 双字符切分
			path = Configuration.getConfig("double-word-lucene-index-file-path");
			break;
		default:
			break;
		}

		// 默认fuzzy查询 jieba index模式
		Directory directory = FSDirectory.open(Paths.get(new File(path).getPath()));

		// 创建indexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		// 创建indexSearcher对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		/** 创建多关键词查询 **/
		BooleanQuery.Builder builder = new BooleanQuery.Builder();

		for (String text : content) {
			TermQuery query = new TermQuery(new Term(field, text));
			// 1．MUST和MUST：取得连个查询子句的交集。
			// 2．MUST和MUST_NOT：表示查询结果中不能包含MUST_NOT所对应得查询子句的检索结果。
			// 3．SHOULD与MUST_NOT：连用时，功能同MUST和MUST_NOT。
			// 4．SHOULD与MUST连用时，结果为MUST子句的检索结果,但是SHOULD可影响排序。
			// 5．SHOULD与SHOULD：表示“或”关系，最终检索结果为所有检索子句的并集。
			// 6．MUST_NOT和MUST_NOT：无意义，检索无结果。
			builder.add(query, Occur.MUST);
		}

		BooleanQuery booleanQuery = builder.build();

		// 普通查询 域名 查询内容
		// TermQuery query = new TermQuery(new Term("abstract", content));

		// 执行查询
		TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
		System.out.println("查询结果的总数" + topDocs.totalHits);
		// 遍历查询结果
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			// scoreDoc.doc 属性就是doucumnet对象的id
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.getValues("id")[0]);
			System.out.println(doc.getValues("title")[0]);
			System.out.println(doc.getValues("abstract")[0]);
		}
		indexReader.close();
	}

	public static void single_search(String field, String content) throws IOException {
		// 制定索引库存放路径
		Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		// 创建indexReader对象
		IndexReader indexReader = DirectoryReader.open(directory);
		// 创建indexSearcher对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		// 普通查询 域名 查询内容
		TermQuery query = new TermQuery(new Term(field, content));
		// 执行查询
		TopDocs topDocs = indexSearcher.search(query, 10);
		System.out.println("查询结果的总数" + topDocs.totalHits);
		// 遍历查询结果
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			// scoreDoc.doc 属性就是doucumnet对象的id
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.getValues("id")[0]);
			System.out.println(doc.getValues("title")[0]);
			System.out.println(doc.getValues("abstract")[0]);
			/** 姓名域变为多值域 输出方法变更 **/
//			String inventorS[] = doc.getValues("inventor");
//			for (String name : inventorS)
//				System.out.println(name);

		}
		indexReader.close();
	}

	// 5bd935865e4c120b8c9787e3
	public static void main(String[] args) throws Exception {
		luceneCreateIndex(SearchAccuracy.FUZZY);
		luceneCreateIndex(SearchAccuracy.ACCURATE);
		luceneCreateIndex(SearchAccuracy.SINGLE_WORD);
		luceneCreateIndex(SearchAccuracy.DOUBLE_WORD);
		// String[] content = { "无","线","电","脑","鼠","标" };
		// multi_search("title", content, 2);
	}
}