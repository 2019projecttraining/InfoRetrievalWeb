package ir.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Service;

import ir.enumDefine.IsGranted;
import ir.enumDefine.PatentTypeCode;
import ir.models.BoolExpression;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.AdvancedSearchService;
import ir.util.seg.AnalyzerToken;

@Service
public class AdvancedSearchServiceImpl implements AdvancedSearchService{

	private static final int pageSize=10;
	@Override
	public PatentsForView search(BoolExpression[] expressions, int page, String timeFrom, String timeTo,
			IsGranted isGranted,PatentTypeCode typeCode,IndexSearcher luceneIndex,Analyzer analyzer) {
		BooleanQuery.Builder builder=new BooleanQuery.Builder();
		
		Query q=null;//日期范围处理成query
        if(timeFrom.equals("NO_LIMIT")&&!timeTo.equals("NO_LIMIT")) {
        	q=TermRangeQuery.newStringRange("application_date", "0000.00.00", timeTo, false, true);//日期范围查询
            builder.add(q, Occur.MUST);
        }else if(!timeFrom.equals("NO_LIMIT")&&timeTo.equals("NO_LIMIT")) {
        	q=TermRangeQuery.newStringRange("application_date", timeFrom , "3000.12.31", true, false);//日期范围查询
            builder.add(q, Occur.MUST);
        }else if(!timeFrom.equals("NO_LIMIT")&&!timeTo.equals("NO_LIMIT")) {
        	q=TermRangeQuery.newStringRange("application_date", timeFrom , timeTo , true, true);//日期范围查询
            builder.add(q, Occur.MUST);
        }
        
        Query q1=null;//是否授权
        if(isGranted==IsGranted.GRANTED) { 
        	q1=new TermQuery(new Term("grant_status", "1"));
        	builder.add(q1, Occur.MUST);
        }
        else if(isGranted==IsGranted.NOT_GRANTED){
        	q1=new TermQuery(new Term("grant_status", "0"));//未通过专利查询
        	builder.add(q1, Occur.MUST);
        }
        
        Query q2=null;//类别限定查询
        switch(typeCode) {
        case ALL:
        	break;
        case A:
        	q2=new TermQuery(new Term("class", "A"));
        	builder.add(q2, Occur.MUST);
        	break;
        case B:
        	q2=new TermQuery(new Term("class", "B"));
        	builder.add(q2, Occur.MUST);
        	break;
        case C:
        	q2=new TermQuery(new Term("class", "C"));
        	builder.add(q2, Occur.MUST);
        	break;
        case D:
        	q2=new TermQuery(new Term("class", "D"));
        	builder.add(q2, Occur.MUST);
        	break;
        case E:
        	q2=new TermQuery(new Term("class", "E"));
        	builder.add(q2, Occur.MUST);
        	break;
        case F:
        	q2=new TermQuery(new Term("class", "F"));
        	builder.add(q2, Occur.MUST);
        	break;
        case G:
        	q2=new TermQuery(new Term("class", "G"));
        	builder.add(q2, Occur.MUST);
        	break;
        case H:
        	q2=new TermQuery(new Term("class", "H"));
        	builder.add(q2, Occur.MUST);
        	break;
        }
        
        //多个与或非表达式处理成query
		for(int i=0;i<expressions.length;i++) {
			BooleanQuery.Builder b=new BooleanQuery.Builder();//每行一个Booleanquery
			List<String> words=new ArrayList<String>();
			words.addAll(AnalyzerToken.token(expressions[i].keyWords,analyzer));
			for(String w:words) {//每个查询语句需分词处理，每个语句中的词得是或关系
				switch(expressions[i].field) {
				case "TITLE":
					b.add(new TermQuery(new Term("title", w)),Occur.MUST);
					break;
				case "ABSTRACT":
					b.add(new TermQuery(new Term("abstract", w)),Occur.MUST);
					break;
				case "APPLICANT":
					b.add(new WildcardQuery(new Term("applicant", "*"+w+"*")),Occur.MUST);
					break;
				case "INVENTOR":
					b.add(new TermQuery(new Term("inventor", w)),Occur.MUST);
					break;
				case "ADDRESS":
					b.add(new WildcardQuery(new Term("address", "*"+w+"*")),Occur.MUST);
					break;
				}
			}
			//为单个BooleanQuery添加到builder中
			switch(expressions[i].symbol) {
			case "AND":
				builder.add(b.build(),Occur.MUST);
				break;
			case "OR":
				builder.add(b.build(),Occur.SHOULD);
				break;
			case "NOT":
				builder.add(b.build(),Occur.MUST_NOT);
				break;
			}
		}
		//执行查询并得到分页起始和末尾数
		TopDocs topDocs=null;
		PatentsForView pv=new PatentsForView();
		List<Patent> patents=new ArrayList<>();
		int start = (page - 1) * pageSize;// 当前页的起始条数
        int end = start + pageSize-1;// 当前页的结束条数（不能包含）
		try {
			topDocs = luceneIndex.search(builder.build(), end+1);
			System.out.println("查询结束，"+"查询结果的总数"+topDocs.totalHits);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			
			if(scoreDocs.length<start) {//越界处理
				page=(scoreDocs.length-1)/pageSize+1;
				start = (page - 1) * pageSize;
				end = start + pageSize-1;
				pv.setPageWhenOutBound(page);
			}
			
			int totalNum=Integer.parseInt(topDocs.totalHits.toString().replaceAll(" hits", "").replaceAll("\\+", ""));
			if(totalNum/pageSize+1==page)//最后一页不一定有pageSize个
				end=totalNum%pageSize+start-1;
			
			//取出返回值
			for (int i = start; i <= end; i++) {
				Document doc=null;
				try {
					doc = luceneIndex.doc(scoreDocs[i].doc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
				p.setGrant_status(doc.get("grant_status").equals("1")?"已授权":"未授权");
				String inventors="";
				for(String s:doc.getValues("inventor")) {
					inventors+=(s+";");
				}
				p.setInventor(inventors);
				p.setTitle(doc.get("title"));
				p.setYear(Integer.parseInt(doc.get("year")));
				
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

}
