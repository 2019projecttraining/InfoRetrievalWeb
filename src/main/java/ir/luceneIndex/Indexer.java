package ir.luceneIndex;


import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.management.Query;
 
import org.apache.commons.io.FileUtils;
import org.apache.commons.csv.*;
import org.apache.commons.csv.CSVFormat.Predefined;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class Indexer {
	public static void luceneCreateIndex() throws Exception{
        //指定索引存放的位置
        Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
        System.out.println("pathname"+Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
        //创建一个分词器
        //StandardAnalyzer analyzer = new StandardAnalyzer();
        //CJKAnalyzer cjkAnalyzer = new CJKAnalyzer();
        SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
        //创建indexwriterConfig(参数分词器)
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(smartChineseAnalyzer);
        //创建indexwriter对象(文件对象,索引配置对象)
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        //原始文件
        //File file = new File("D:\\Lucene_Path\\test1");

        //读csv
        String[] headers = new String[] {"id","abstract","address","applicant",
        		"application_date","application_number","application_publish_number",
        		"classification_number","filing_date","grant_status",
        		"inventor","title","year"};
        String csvPath = "D:\\Lucene_Path\\patent_new.csv";
        
        FileReader reader = new FileReader(csvPath);
        CSVParser parser = CSVFormat.DEFAULT.withHeader(headers).parse(reader);
        
        for (CSVRecord record : parser)
        {
           System.out.println(record.get("title"));
           
           

           //preserved_type保护域不分词
           //default_type默认分词
           //分词模式DOCS
           FieldType preserved_type = new FieldType();
           preserved_type.setTokenized(false);
           preserved_type.setStored(true);
           preserved_type.setIndexOptions(IndexOptions.DOCS);
           FieldType default_type = new FieldType();
           default_type.setTokenized(true);
           default_type.setStored(true);
           default_type.setIndexOptions(IndexOptions.DOCS);

           //创建文件域名
           //域的名称 域的内容 fieldtype
           Field id_field = new Field("id", record.get("id"), preserved_type);
           Field abstract_field = new Field("abstract", record.get("abstract"), default_type);
           Field address_field = new Field("address", record.get("address"), default_type);
           Field applicant_field = new Field("applicant", record.get("applicant"), preserved_type);
           
           Field application_date_field = new Field("application_date", record.get("application_date"), preserved_type);
           Field application_number_field = new Field("application_number", record.get("application_number"), preserved_type);
           Field application_publish_number_field = new Field("application_publish_number", record.get("application_publish_number"), preserved_type);
           
           Field classification_number_field = new Field("classification_number", record.get("classification_number"),preserved_type);
           Field filing_date_field = new Field("filing_date", record.get("filing_date"), preserved_type);
           Field grant_status_field = new Field("grant_status", record.get("grant_status"), preserved_type);
           
           Field inventor_field = new Field("inventor", record.get("inventor"), preserved_type);
           Field title_field = new Field("title", record.get("title"), default_type);
           Field year_field = new Field("year", record.get("year"), preserved_type);


           //System.out.println(lines[11]);
           //System.out.println(lines[3]);
           //System.out.println(record.get("year"));
           
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
           
           indexableFields.add(inventor_field);
           indexableFields.add(title_field);
           indexableFields.add(year_field);
           
           //indexableFields.add(fileSizeField);
           //创建索引 并写入索引库
           indexWriter.addDocument(indexableFields);
           
        }
        reader.close();
        //关闭indexWriter
        indexWriter.close();
    }
	public static void searchIndex(String content) throws IOException {
        //制定索引库存放路径
        //E:\Lucene_Path\Lucene_index
        Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
        //创建indexReader对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建indexSearcher对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        
        /**创建多条件查询**/
        //BooleanQuery query = new BooleanQuery();
        /*String searchField="INVENTOR";
        String q1="李";
        String q2="睿";
        String q3="智";
        TermQuery query1=new TermQuery(new Term(searchField,q1));
        TermQuery query2=new TermQuery(new Term(searchField,q2));
        TermQuery query3=new TermQuery(new Term(searchField,q3));
        BooleanQuery.Builder  builder=new BooleanQuery.Builder();
        // 1．MUST和MUST：取得连个查询子句的交集。
        // 2．MUST和MUST_NOT：表示查询结果中不能包含MUST_NOT所对应得查询子句的检索结果。
        // 3．SHOULD与MUST_NOT：连用时，功能同MUST和MUST_NOT。
        // 4．SHOULD与MUST连用时，结果为MUST子句的检索结果,但是SHOULD可影响排序。
        // 5．SHOULD与SHOULD：表示“或”关系，最终检索结果为所有检索子句的并集。
        // 6．MUST_NOT和MUST_NOT：无意义，检索无结果。
        builder.add(query1, Occur.MUST);
        builder.add(query2, Occur.MUST);
        builder.add(query3, Occur.MUST);
        BooleanQuery booleanQuery=builder.build();*/

        //普通查询 域名 查询内容
        TermQuery query = new TermQuery(new Term("abstract", content));

        
        //执行查询
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("查询结果的总数"+topDocs.totalHits);
        //遍历查询结果
        for (ScoreDoc scoreDoc: topDocs.scoreDocs){
            //scoreDoc.doc 属性就是doucumnet对象的id
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.getField("id"));
            System.out.println(doc.getField("abstract"));
            System.out.println(doc.getField("inventor"));
            //System.out.println(doc.getField("ABSTRACT"));
            //System.out.println(doc.getField("INVENTOR"));
            /*System.out.println(doc.getField("fileName"));
            System.out.println(doc.getField("fileContent"));
            System.out.println(doc.getField("filePath"));
            System.out.println(doc.getField("fileSize"));*/
        }
        indexReader.close();
    }
	public static void main(String[] args) throws Exception {
		//luceneCreateIndex();
		searchIndex("电脑");
	}
}
