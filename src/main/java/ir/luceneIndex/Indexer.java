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

import net.sourceforge.pinyin4j.PinyinHelper;

public class Indexer {
	public static void luceneCreateIndex() throws Exception {
		// ָ��������ŵ�λ��
		Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		System.out.println("pathname" + Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		// ����һ���ִ���
		// StandardAnalyzer analyzer = new StandardAnalyzer();
		// CJKAnalyzer cjkAnalyzer = new CJKAnalyzer();
		// �������ķִ���
		SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
		// ����indexwriterConfig(�����ִ���)
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(smartChineseAnalyzer);
		// ����indexwriter����(�ļ�����,�������ö���)
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		// ��csv
		String[] headers = new String[] { "id", "abstract", "address", "applicant", "application_date",
				"application_number", "application_publish_number", "classification_number", "filing_date",
				"grant_status", "inventor", "title", "year" };
		String csvPath = "D:\\Lucene_Path\\patent_new.csv";

		FileReader reader = new FileReader(csvPath);
		CSVParser parser = CSVFormat.DEFAULT.withHeader(headers).parse(reader);

		for (CSVRecord record : parser) {

			System.out.println(record.get("title"));
			// preserved_type�����򲻷ִ�
			// default_typeĬ�Ϸִ�
			// �ִ�ģʽDOCS
			FieldType preserved_type = new FieldType();
			preserved_type.setTokenized(false);
			preserved_type.setStored(true);
			preserved_type.setIndexOptions(IndexOptions.DOCS);
			FieldType default_type = new FieldType();
			default_type.setTokenized(true);
			default_type.setStored(true);
			default_type.setIndexOptions(IndexOptions.DOCS);

			// �����ļ�����
			// ������� ������� fieldtype
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

			// ����������Լ�����ĸ��
			String inventorS = record.get("inventor");
			String inventor[] = inventorS.split(";");
			for (String out : inventor) {
				String firstW="";
				System.out.println(out);
				if (out.charAt(0) >= 'A' && out.charAt(0) <= 'Z' || out.charAt(0) >= 'a' && out.charAt(0) <= 'z') {
					firstW = String.valueOf(out.charAt(0));
					// System.out.println(firstW);
				} else {
					String temp[] = PinyinHelper.toHanyuPinyinStringArray(out.charAt(0));
					if(temp!=null) firstW = String.valueOf(temp[0].charAt(0));
					System.out.println(firstW);
				}
				firstW = firstW.toUpperCase();
				// ������
				Field inventor_field = new Field("inventor", out, preserved_type);
				// ����ĸ��FirstWorld
				Field inventor_firstW_field = new Field("inventor_firstW", firstW, preserved_type);

				indexableFields.add(inventor_field);
				indexableFields.add(inventor_firstW_field);
			}

			// indexableFields.add(fileSizeField);
			// �������� ��д��������
			indexWriter.addDocument(indexableFields);

		}
		reader.close();
		// �ر�indexWriter
		indexWriter.close();
	}

	// Ĭ�ϵĶ�������ѯ
	public static void boolean_search(String content) throws IOException {
		// �ƶ���������·��
		// E:\Lucene_Path\Lucene_index
		Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		// ����indexReader����
		IndexReader indexReader = DirectoryReader.open(directory);
		// ����indexSearcher����
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		/** ������������ѯ **/
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
		// 1��MUST��MUST��ȡ��������ѯ�Ӿ�Ľ�����
		// 2��MUST��MUST_NOT����ʾ��ѯ����в��ܰ���MUST_NOT����Ӧ�ò�ѯ�Ӿ�ļ��������
		// 3��SHOULD��MUST_NOT������ʱ������ͬMUST��MUST_NOT��
		// 4��SHOULD��MUST����ʱ�����ΪMUST�Ӿ�ļ������,����SHOULD��Ӱ������
		// 5��SHOULD��SHOULD����ʾ���򡱹�ϵ�����ռ������Ϊ���м����Ӿ�Ĳ�����
		// 6��MUST_NOT��MUST_NOT�������壬�����޽����
		builder.add(query1, Occur.SHOULD);
		builder.add(query2, Occur.SHOULD);
		builder.add(query3, Occur.SHOULD);
		builder.add(query4, Occur.SHOULD);
		builder.add(query5, Occur.SHOULD);
		builder.add(query6, Occur.SHOULD);
		BooleanQuery booleanQuery = builder.build();

		// ��ͨ��ѯ ���� ��ѯ����
		// TermQuery query = new TermQuery(new Term("abstract", content));

		// ִ�в�ѯ
		TopDocs topDocs = indexSearcher.search(booleanQuery, 10);
		System.out.println("��ѯ���������" + topDocs.totalHits);
		// ������ѯ���
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			// scoreDoc.doc ���Ծ���doucumnet�����id
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

	public static void single_search(String field, String content) throws IOException {
		// �ƶ���������·��
		// E:\Lucene_Path\Lucene_index
		Directory directory = FSDirectory.open(Paths.get(new File("D:\\Lucene_Path\\Lucene_index").getPath()));
		// ����indexReader����
		IndexReader indexReader = DirectoryReader.open(directory);
		// ����indexSearcher����
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		// ��ͨ��ѯ ���� ��ѯ����
		TermQuery query = new TermQuery(new Term(field, content));

		// ִ�в�ѯ
		TopDocs topDocs = indexSearcher.search(query, 10);
		System.out.println("��ѯ���������" + topDocs.totalHits);
		// ������ѯ���
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			// scoreDoc.doc ���Ծ���doucumnet�����id
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.getField("id"));
			System.out.println(doc.getField("abstract"));
			String inventorS[] = doc.getValues("inventor");
			for (String name : inventorS)
				System.out.println(name);

		}
		indexReader.close();
	}

	public static void main(String[] args) throws Exception {
		//luceneCreateIndex();
		single_search("inventor_firstW","A");
	}
}