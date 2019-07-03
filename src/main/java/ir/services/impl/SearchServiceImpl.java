package ir.services.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
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

import org.apache.lucene.search.highlight.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.enumDefine.FieldType;
import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.seg.AnalyzerToken;
import ir.util.w2v.SimilarWords;
import ir.util.w2v.WordEntry;
import ir.util.w2v.WordHashMap;

/**
 * 实现查询业务
 * 
 * @author 余定邦、杨涛、孙晓军
 *
 */
@Service
public class SearchServiceImpl implements SearchService{

	@Autowired
	private SimilarWords similarWords;
	@Autowired
	private WordHashMap wordHashMap;
	
	private static final int pageSize=10;

	@Override
	public PatentsForView search(FieldType field , String keyWords, int page, FirstLetterOfNamePinyin letter, 
			String timeFrom, String timeTo,IsGranted isGranted, SortedType sortedType,IndexSearcher luceneIndex ,Analyzer analyzer){
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
		words=AnalyzerToken.token(keyWords,analyzer);
		System.out.println(words);
		//获取近义词
		Map<String,List<WordEntry>> wordMap=new LinkedHashMap<String,List<WordEntry>>();
		for(String w:words) {
			List<WordEntry> s=null;
			s=wordHashMap.getNearWord(w);
			wordMap.put(w,s);
		}
		System.out.println(wordMap);
		
		Map<String, Float> boosts = new HashMap<>();
		boosts.put("title", 1.2f);
		boosts.put("inventor", 4.1f);
		boosts.put("abstract", 1.0f);
		boosts.put("applicant", 0.9f);
		
        String[] fields = {"abstract", "applicant" , "title" , "inventor"};
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
			for(String word:words) {//在摘要中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				BooleanQuery.Builder b=new BooleanQuery.Builder();
				b.add(new TermQuery(new Term("title", word)),Occur.SHOULD);//添加原词query查询，关系为或
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("title", w.name));
					b.add(query, Occur.SHOULD);//添加近义词query查询，关系为或
				}
				BooleanQuery nearWordQuery=b.build();
				builder.add(nearWordQuery,Occur.MUST);//每组（原词+其近义词）查询间的关系为且
			}
			break;
		case ABSTRACT:
			for(String word:words) {//在摘要中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				BooleanQuery.Builder b=new BooleanQuery.Builder();
				b.add(new TermQuery(new Term("abstract", word)),Occur.SHOULD);//添加原词query查询，关系为或
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("abstract", w.name));
					b.add(query, Occur.SHOULD);//添加近义词query查询，关系为或
				}
				BooleanQuery nearWordQuery=b.build();
				builder.add(nearWordQuery,Occur.MUST);//每组（原词+其近义词）查询间的关系为且
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
			
			//下面为高亮
			SimpleHTMLFormatter formatter=new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
            Highlighter highlighter=new Highlighter(formatter, new QueryScorer(booleanQuery));
            highlighter.setTextFragmenter(new SimpleFragmenter(400));
			
			for (int i = start; i <= end; i++) {
				Document doc = luceneIndex.doc(scoreDocs[i].doc);
				String titleContent=doc.get("title");
				TokenStream tokenstream=analyzer.tokenStream(keyWords, new StringReader(titleContent));
				try {
					titleContent=highlighter.getBestFragment(tokenstream, titleContent);
					if(titleContent==null)
						titleContent=doc.get("title");
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}
				
				Patent p=new Patent();
				p.setId(doc.get("id"));
				p.setPatent_Abstract(doc.get("abstract"));
				p.setAddress(doc.get("address"));
				p.setApplicant(doc.get("applicant"));

				p.setApplication_date(doc.get("application_date"));
				p.setApplication_number(doc.get("application_number"));
				
				p.setApplication_publish_number(doc.get("application_publish_number"));
				p.setClassification_number(doc.get("classification_number"));
				p.setFilling_date(doc.get("filing_date"));
				p.setGrant_status(Integer.parseInt(doc.get("grant_status")));
				String inventors="";
				for(String s:doc.getValues("inventor")) {
					inventors+=(s+";");
				}
				p.setInventor(inventors);
				p.setTitle(titleContent);
				p.setYear(Integer.parseInt(doc.get("year")));

				patents.add(p);
			}
			pv.setPatents(patents);
			pv.setHitsNum(topDocs.totalHits.toString().replaceAll(" hits", ""));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return pv;
	}
}
