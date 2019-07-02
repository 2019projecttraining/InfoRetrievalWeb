package ir.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
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

import ir.enumDefine.FieldType;
import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.seg.SegmentAnalyzer;
import ir.util.w2v.SimilarWords;
import ir.util.w2v.WordEntry;
import ir.util.w2v.WordHashMap;

/**
 * 实现查询业务
 * 
 * @author 余定邦、杨涛
 *
 */
@Service
public class SearchServiceImpl implements SearchService{

	@Autowired
	private SimilarWords similarWords;
	@Autowired
	private WordHashMap wordHashMap;
	
	private Analyzer analyzer=SegmentAnalyzer.coarseGrainedAnaylzer;
	private static final int pageSize=10;

	@Override
	public PatentsForView search(FieldType field , String keyWords, int page, FirstLetterOfNamePinyin letter, 
			String timeFrom, String timeTo,IsGranted isGranted, SortedType sortedType,IndexSearcher luceneIndex){
		//TODO
		
        BooleanQuery.Builder builder=new BooleanQuery.Builder();

        Query q1=null;//是否授权
        if(isGranted==IsGranted.GRANTED) { 
        	q1=new TermQuery(new Term("grant_status", "1"));//通过专利查询
        	builder.add(q1, Occur.MUST);
        }
        else if(isGranted==IsGranted.NOT_GRANTED){
        	q1=new TermQuery(new Term("grant_status", "0"));//未通过专利查询
        	builder.add(q1, Occur.MUST);
        }
        Query q2=null;//日期范围
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
        Query q3=null;//姓名首字母
        if(letter!=FirstLetterOfNamePinyin.NO_LIMIT) {
        	q3=new TermQuery(new Term("inventor_firstW", letter.toString()));
        	builder.add(q3, Occur.MUST);
        }
        
        //近义词查询
		List<String> words = null;
		try {
			words=analyze(keyWords,analyzer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//获取近义词
		Map<String,List<WordEntry>> wordMap=new LinkedHashMap<String,List<WordEntry>>();
		for(String w:words) {
			System.out.println(w);
			List<WordEntry> s=null;
			s=wordHashMap.getNearWord(w);
			System.out.println(s);
			wordMap.put(w,s);
		}
		
        String[] fields = { "inventor","abstract", "applicant" , "title"};
		Query keyQuery;
		switch(field) {//按域查询
		case ALL:
			try {
				keyQuery = new MultiFieldQueryParser(fields, analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			break;			
		case TITLE:
			try {
				keyQuery=new QueryParser("title", analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			for(String word:words) {//在标题中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("title", w.name));
					builder.add(query, Occur.SHOULD);
				}
			}
			break;
		case ABSTRACT:
			try {
				keyQuery=new QueryParser("abstract", analyzer).parse(keyWords);
				builder.add(keyQuery, Occur.MUST);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			for(String word:words) {//在摘要中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("abstract", w.name));
					builder.add(query, Occur.SHOULD);
				}
			}
			break;
			
		case APPLICANT:
			keyQuery=new WildcardQuery(new Term("applicant", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		case ID:
			keyQuery=new TermQuery(new Term("application_publish_number", keyWords));
			builder.add(keyQuery, Occur.MUST);
			break;
		case INVENTOR:
			keyQuery=new WildcardQuery(new Term("inventor", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		case ADDRESS:
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
			topDocs = luceneIndex.search(booleanQuery, end+1);
			System.out.println("查询结束");
	        System.out.println("查询结果的总数"+topDocs.totalHits);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			int totalNum=Integer.parseInt(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			if(totalNum/pageSize+1==page)//最后一页不一定有pageSize个
				end=totalNum%pageSize+start-1;
			
			for (int i = start; i <= end; i++) {
				Document doc = luceneIndex.doc(scoreDocs[i].doc);

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
				String inventors="";
				for(String s:doc.getValues("inventor")) {
					inventors+=(s+";");
				}
				p.setInventor(inventors);
				p.setTitle(doc.getField("title").getCharSequenceValue().toString());
				p.setYear(Integer.parseInt(doc.getField("year").getCharSequenceValue().toString()));

				patents.add(p);
			}
			pv.setPatents(patents);
			pv.setHitsNum(topDocs.totalHits.toString().replaceAll(" hits", ""));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pv;
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
