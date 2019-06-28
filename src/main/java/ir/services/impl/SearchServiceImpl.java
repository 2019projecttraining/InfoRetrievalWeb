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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.luceneIndex.LuceneSearcher;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.seg.SegmentAnalyzer;
import ir.util.w2v.SimilarWords;
import ir.util.w2v.WordEntry;

/**
 * 实现查询业务
 * 
 * @author 余定邦、杨涛
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
	public PatentsForView search(String field, String keyWords, int page, FirstLetterOfNamePinyin letter, 
			String timeFrom, String timeTo,IsGranted isGranted, SortedType sortedType){
		//TODO
		
        BooleanQuery.Builder builder=new BooleanQuery.Builder();

        Query q1=null;
        if(isGranted==IsGranted.GRANTED) { 
        	q1=new TermQuery(new Term("grant_status", "1"));//通过专利查询
        	builder.add(q1, Occur.MUST);
        }
        else if(isGranted==IsGranted.NOT_GRANTED){
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
        
        String[] fields = { "abstract", "applicant" , "title" , "inventor" };
        
//		List<String> words = null;
//		try {
//			words=analyze(keyWords,analyzer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		Query keyQuery;
		switch(field) {
		case "all":
			try {
				keyQuery = new MultiFieldQueryParser(fields, analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			} 
			break;
		case "title":
			try {
				keyQuery=new QueryParser("title", analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			break;
		case "abstract":
			try {
				keyQuery=new QueryParser("abstract", analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			break;
		case "applicant":
			keyQuery=new WildcardQuery(new Term("applicant", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		case "application_publish_number":
			keyQuery=new TermQuery(new Term("application_publish_number", keyWords));
			builder.add(keyQuery, Occur.MUST);
			break;
		case "inventor":
			keyQuery=new WildcardQuery(new Term("inventor", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		case "address":
			keyQuery=new WildcardQuery(new Term("address", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		}
		 
		
        BooleanQuery booleanQuery=builder.build();
        Sort sort=null;
        if(sortedType==SortedType.TIME_ASC) {
        	sort = new Sort(new SortField("filing_date", Type.STRING, false));
        }else if(sortedType==SortedType.TIME_DESC) {
        	sort = new Sort(new SortField("filing_date", Type.STRING, true));
        }
        
		TopDocs topDocs=null;
		PatentsForView pv=new PatentsForView();
		List<Patent> patents=new ArrayList<>();
		try {
			//if(sort!=null)
			int start = (page - 1) * pageSize;// 当前页的起始条数
	        int end = start + pageSize-1;// 当前页的结束条数（不能包含）
			topDocs = luceneSearcher.search(booleanQuery, end+1);
			System.out.println("查询结束");
	        System.out.println("查询结果的总数"+topDocs.totalHits);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			int totalNum=Integer.parseInt(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			System.out.println("zheli");
			if(totalNum/pageSize+1==page)//最后一页不一定有pageSize个
				end=totalNum%pageSize+start-1;
			
			for (int i = start; i <= end; i++) {
				System.out.println(start+" "+end);
				Document doc = luceneSearcher.doc(scoreDocs[i].doc);
				System.out.println("aaaavvvcdzscaaaa");

				Patent p=new Patent();
				p.setId(doc.getField("id").getCharSequenceValue().toString());
				p.setPatent_Abstract(doc.getField("abstract").getCharSequenceValue().toString());
				p.setAddress(doc.getField("address").getCharSequenceValue().toString());
				p.setApplicant(doc.getField("applicant").getCharSequenceValue().toString());

				p.setApplication_date(doc.getField("application_date").getCharSequenceValue().toString());
				p.setApplication_number(doc.getField("application_number").getCharSequenceValue().toString());
				
				p.setApplication_publish_number(doc.getField("application_publish_number").getCharSequenceValue().toString());
				p.setClassification_number(doc.getField("classification_number").getCharSequenceValue().toString());
				p.setFilling_date(doc.getField("filing_date").getCharSequenceValue().toString());
				p.setGrant_status(Integer.parseInt(doc.getField("grant_status").getCharSequenceValue().toString()));
				p.setInventor(doc.getField("inventor").getCharSequenceValue().toString());
				p.setTitle(doc.getField("title").getCharSequenceValue().toString());
				p.setYear(Integer.parseInt(doc.getField("year").getCharSequenceValue().toString()));
				System.out.println("aaaaaaaaaaa");

				patents.add(p);
			}
			pv.setPatents(patents);
			System.out.println("bbbbbbbbbb");
			pv.setHitsNum(topDocs.totalHits.toString().replaceAll(" hits", ""));

//			for (ScoreDoc scoreDoc: topDocs.scoreDocs){
//	            //scoreDoc.doc 属性就是doucumnet对象的id
//	            Document doc = luceneSearcher.doc(scoreDoc.doc);	            	   
//	            sb.append(doc.getField("id").toString().replaceAll("[<|>]", "")).append("<br/>");
//	            sb.append(doc.getField("abstract").toString().replaceAll("[<|>]", "")).append("<br/>");
//	            sb.append(doc.getField("inventor").toString().replaceAll("[<|>]", "")).append("<br/>");
//	            sb.append(doc.getField("filing_date").toString().replaceAll("[<|>]", "")).append("<br/>");
//	            sb.append(doc.getField("grant_status").toString().replaceAll("[<|>]", "")).append("<br/>");
//	            sb.append("<br/><br/>")
//	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pv;
		
		
		
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
}
