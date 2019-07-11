//package ir.luceneIndex;
//
//import java.io.File;
//import java.io.FileReader;
//import java.nio.file.Paths;
//
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.FieldType;
//import org.apache.lucene.index.IndexOptions;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.FSDirectory;
//
//import ir.util.seg.SegmentAnalyzer;
//import net.sourceforge.pinyin4j.PinyinHelper;
//
//public class HanLPIndex {
//	
//	public static void main(String[] args) throws Exception {
//		luceneCreateIndex(SegmentAnalyzer.DEFALUT_COARSE_GRAINED_ANALYZER,"C:\\Users\\HPuser\\Desktop\\ir-data\\hanlp_coarse_index","D:\\data\\newPatent.csv");
//	}
//
//	public static void luceneCreateIndex(Analyzer analyzer,String toFilePath,String fromFile) throws Exception {
//		// 指定索引存放的位置
//		Directory directory = FSDirectory.open(Paths.get(new File(toFilePath).getPath()));
//		//System.out.println("pathname" + Paths.get(new File(toFilePath).getPath()));
//		// 创建一个分词器
//		// StandardAnalyzer analyzer = new StandardAnalyzer();
//		// CJKAnalyzer cjkAnalyzer = new CJKAnalyzer();
//		// 智能中文分词器
//		// SmartChineseAnalyzer smartChineseAnalyzer = new SmartChineseAnalyzer();
//		//JiebaAnalyzer jiebaAnalyzer = new JiebaAnalyzer(JiebaSegmenter.SegMode.INDEX);
//		// 创建indexwriterConfig(参数分词器)
//		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
//		// 创建indexwriter对象(文件对象,索引配置对象)
//		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
//		// 读csv
//		String[] headers = new String[] { "id", "abstract", "address", "applicant", "application_date",
//				"application_number", "application_publish_number", "classification_number", "filing_date",
//				"grant_status", "inventor", "title", "year" };
//		String csvPath = fromFile;
//
//		FileReader reader = new FileReader(csvPath);
//		CSVParser parser = CSVFormat.DEFAULT.withHeader(headers).parse(reader);
//
//		int count=0;
//		for (CSVRecord record : parser) {
//
//			++count;
//			if(count%10000==0)
//				System.out.println(count/10000+"w");
//			
//			//System.out.println(record.get("title"));
//			// preserved_type保护域不分词
//			// default_type默认分词
//			// 分词模式DOCS
//			FieldType preserved_type = new FieldType();
//			preserved_type.setTokenized(false);
//			preserved_type.setStored(true);
//			preserved_type.setIndexOptions(IndexOptions.DOCS);
//			FieldType default_type = new FieldType();
//			default_type.setTokenized(true);
//			default_type.setStored(true);
//			default_type.setIndexOptions(IndexOptions.DOCS);
//
//			// 创建文件域名
//			// Field(域的名称,域的内容,fieldtype)
//			Field id_field = new Field("id", record.get("id"), preserved_type);
//			Field abstract_field = new Field("abstract", record.get("abstract"), default_type);
//			Field address_field = new Field("address", record.get("address"), default_type);
//			Field applicant_field = new Field("applicant", record.get("applicant"), preserved_type);
//
//			Field application_date_field = new Field("application_date", record.get("application_date"),
//					preserved_type);
//			Field application_number_field = new Field("application_number", record.get("application_number"),
//					preserved_type);
//			Field application_publish_number_field = new Field("application_publish_number",
//					record.get("application_publish_number"), preserved_type);
//
//			Field classification_number_field = new Field("classification_number", record.get("classification_number"),
//					preserved_type);
//			Field filing_date_field = new Field("filing_date", record.get("filing_date"), preserved_type);
//			Field grant_status_field = new Field("grant_status", record.get("grant_status"), preserved_type);
//
//			// Field inventor_field = new Field("inventor", record.get("inventor"),
//			// preserved_type);
//			Field title_field = new Field("title", record.get("title"), default_type);
//			Field year_field = new Field("year", record.get("year"), preserved_type);
//
//			Document indexableFields = new Document();
//			indexableFields.add(id_field);
//			indexableFields.add(abstract_field);
//			indexableFields.add(address_field);
//			indexableFields.add(applicant_field);
//
//			indexableFields.add(application_date_field);
//			indexableFields.add(application_number_field);
//			indexableFields.add(application_publish_number_field);
//
//			indexableFields.add(classification_number_field);
//			indexableFields.add(filing_date_field);
//			indexableFields.add(grant_status_field);
//
//			// indexableFields.add(inventor_field);
//			indexableFields.add(title_field);
//			indexableFields.add(year_field);
//
//			// 添加姓名域以及首字母域
//			// 姓名域改为多值域
//			String inventorS = record.get("inventor");
//			String inventor[] = inventorS.split(";");
//			
//			for (String out : inventor) {
//				String firstW = "";
//				//System.out.println(out);
//				if (out.charAt(0) >= 'A' && out.charAt(0) <= 'Z' || out.charAt(0) >= 'a' && out.charAt(0) <= 'z') {
//					firstW = String.valueOf(out.charAt(0));
//					// System.out.println(firstW);
//				} else {
//					String temp[] = PinyinHelper.toHanyuPinyinStringArray(out.charAt(0));
//					if (temp != null)
//						firstW = String.valueOf(temp[0].charAt(0));
//					//System.out.println(firstW);
//				}
//				firstW = firstW.toUpperCase();
//				// 姓名域
//				Field inventor_field = new Field("inventor", out, preserved_type);
//				// 首字母域FirstWorld
//				Field inventor_firstW_field = new Field("inventor_firstW", firstW, preserved_type);
//
//				indexableFields.add(inventor_field);
//				indexableFields.add(inventor_firstW_field);
//			}
//
//			// indexableFields.add(fileSizeField);
//			// 创建索引 并写入索引库
//			indexWriter.addDocument(indexableFields);
//
//		}
//		reader.close();
//		// 关闭indexWriter
//		indexWriter.close();
//	}
//	
//}
