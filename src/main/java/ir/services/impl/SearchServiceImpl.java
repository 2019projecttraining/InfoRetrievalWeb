package ir.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.luceneIndex.LuceneSearcher;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.seg.SegmentAnalyzer;
import ir.util.w2v.SimilarWords;
import ir.util.w2v.WordEntry;

/**
 * 实现查询业务
 * 
 * @author 余定邦
 *
 */
@Service
public class SearchServiceImpl implements SearchService{

	@Autowired
	private LuceneSearcher luceneSearcher;
	@Autowired
	private SimilarWords similarWords;
	
	private Analyzer analyzer=SegmentAnalyzer.anaylzer;
	private static final int pageSize=10; 

	@Override
	public PatentsForView search(String keyWords, int page, FirstLetterOfNamePinyin letter, 
			String timeFrom, String timeTo,IsGranted isGranted, SortedType sortedType){
		//TODO
		int start = (page - 1) * pageSize;// 当前页的起始条数
        int end = start + pageSize;// 当前页的结束条数（不能包含）
        BooleanQuery.Builder builder=new BooleanQuery.Builder();

        Query q1=null;
        if(isGranted==IsGranted.GRANTED) { 
        	q1=new TermQuery(new Term("grant_status", "1"));//通过专利查询
        	builder.add(q1, Occur.MUST);
        }
        else {
        	q1=new TermQuery(new Term("grant_status", "0"));//未通过专利查询
        	builder.add(q1, Occur.MUST);
        	}
        Query q2=null;
        if(timeFrom.equals("NO_LIMIT")&&!timeTo.equals("NO_LIMIT")) {
        	q2=TermRangeQuery.newStringRange("filing_date", "0000.00.00", timeTo, false, true);//日期范围查询
            builder.add(q2, Occur.MUST);
        }else if(!timeFrom.equals("NO_LIMIT")&&timeTo.equals("NO_LIMIT")) {
        	q2=TermRangeQuery.newStringRange("filing_date", timeFrom , "3000.12.31", true, false);//日期范围查询
            builder.add(q2, Occur.MUST);
        }else if(!timeFrom.equals("NO_LIMIT")&&!timeTo.equals("NO_LIMIT")) {
        	q2=TermRangeQuery.newStringRange("filing_date", timeFrom , timeTo , true, false);//日期范围查询
            builder.add(q2, Occur.MUST);
        }
        
        String[] fields = { "abstract", "applicant", "title"};
        
		List<String> words = null;
		try {
			words=analyze(keyWords,analyzer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Query multiFieldQuery;
		try {
			multiFieldQuery = new MultiFieldQueryParser(fields, analyzer).parse(keyWords);
			builder.add(multiFieldQuery, Occur.MUST);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}  
        BooleanQuery booleanQuery=builder.build();

		TopDocs topDocs=null;
		try {
			topDocs = luceneSearcher.search(booleanQuery, end);
			for (ScoreDoc scoreDoc: topDocs.scoreDocs){
	            //scoreDoc.doc 属性就是doucumnet对象的id
	            Document doc = luceneSearcher.doc(scoreDoc.doc);
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("查询结果的总数"+topDocs.totalHits);
		
		
		
		
//		List<List<WordEntry>> twoDlist=new ArrayList<List<WordEntry>>();
//		for(String w:words) {
//			Set<WordEntry> s=null;
//			try {
//				s=similarWords.getSimilarWordsByLimit(w, 0.5);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			twoDlist.add(new ArrayList<WordEntry>(s));
//		}
//		
//		for(List<WordEntry> l:twoDlist) {
//			for(WordEntry w:l) {
//				TermQuery query1 = new TermQuery(new Term("abstract", w.name));
//				TermQuery query2 = new TermQuery(new Term("applicant", w.name));
//				TermQuery query3 = new TermQuery(new Term("title", w.name));
//				builder.add(query1, Occur.SHOULD).;
//			}
//		}

		
		return null;
	}
	
	public static List<String> analyze(String text, Analyzer analyzer) throws IOException{
	    List<String> result = new ArrayList<String>();
	    TokenStream tokenStream = analyzer.tokenStream("KeyWords", text);
	    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
	    tokenStream.reset();
	    while(tokenStream.incrementToken()) {
	       result.add(attr.toString());
	    }       
	    return result;
	}
	public static void main(String[] args) {
		SearchServiceImpl s=new SearchServiceImpl();
		s.search("", 2, FirstLetterOfNamePinyin.A, "2012.01.02", "2018.09.02", IsGranted.NO_LIMIT, SortedType.COMPREHENSIVENESS);
	}
}
