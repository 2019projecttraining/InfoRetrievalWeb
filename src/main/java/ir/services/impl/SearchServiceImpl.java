package ir.services.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DoubleValues;
import org.apache.lucene.search.DoubleValuesSource;
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
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ir.enumDefine.FieldType;
import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.fieldDetection.ApplicantDetection;
import ir.util.fieldDetection.ApplicationPublishNumberDetection;
import ir.util.fieldDetection.InventorDetection;
import ir.util.seg.AnalyzerToken;
import ir.util.ssc_fix.WrongWordAnalyzer;
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
	private WordHashMap wordHashMap;
	
	private static final int pageSize=10;

	@Override
	public PatentsForView search(FieldType field , String keyWords, int page, FirstLetterOfNamePinyin letter, 
			String timeFrom, String timeTo,IsGranted isGranted, SortedType sortedType,IndexSearcher luceneIndex ,Analyzer analyzer){
		
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
        
        //分词存放数据结构
		List<String> words = null;
		//近义词存放数据结构
		Map<String,List<WordEntry>> wordMap=null;

		
        //String[] fields = {"abstract", "applicant" , "title" , "inventor",""};
		Query keyQuery;
		switch(field) {//按域查询
		case ALL:
			words=Arrays.asList(keyWords.split(" "));//按空格分词
			words=wordReplace(words,field);//替换错别字
			List<String> words2=new ArrayList<String>();//再分词存放
			BooleanQuery.Builder b1 = new BooleanQuery.Builder();
			BooleanQuery.Builder b2 = new BooleanQuery.Builder();
			BooleanQuery.Builder b3 = new BooleanQuery.Builder();
			BooleanQuery.Builder b4 = new BooleanQuery.Builder();

			System.out.println(words);
			int lock1=0,lock2=0,lock3=0,lock4=0;
			for(String word:words) {
				if(InventorDetection.isInventor(word)) {
					lock1=1;
					b1.add(new TermQuery(new Term("inventor", word)),Occur.SHOULD);
				}else if(ApplicationPublishNumberDetection.isApplicationPublishNumber(word)) {
					lock2=1;
					b2.add(new TermQuery(new Term("application_publish_number", word)),Occur.SHOULD);
				}else if(ApplicantDetection.isCompanyApplicant(word)){//申请人如果是公司的话，在摘要中也添加查询
					lock3=1;
					b3.add(new TermQuery(new Term("abstract", word)),Occur.SHOULD);
					b3.add(new TermQuery(new Term("title", word)),Occur.SHOULD);
					b3.add(new WildcardQuery(new Term("applicant", "*"+word+"*")),Occur.SHOULD);
				}else if(ApplicantDetection.isPeopleApplicant(word)){
					lock4=1;
					b4.add(new TermQuery(new Term("applicant", word)),Occur.SHOULD);
				}else {//对不是人名或专利号的词再分词
					words2.addAll(AnalyzerToken.token(word,analyzer));
				}
			}
			System.out.println(words2);
			if(lock1==1) {
				BooleanQuery build1=b1.build();
				builder.add(build1, Occur.MUST);
			}
			if(lock2==1) {
				BooleanQuery build2=b2.build();
				builder.add(build2, Occur.MUST);
			}
			if(lock3==1) {
				BooleanQuery build3=b3.build();
				builder.add(build3, Occur.MUST);
			}
			if(lock4==1) {
				BooleanQuery build4=b4.build();
				builder.add(build4, Occur.MUST);
			}
			for(String word:words2) {
				BooleanQuery.Builder titleOrAbstract = new BooleanQuery.Builder();
				titleOrAbstract.add(new TermQuery(new Term("abstract", word)),Occur.SHOULD);
				titleOrAbstract.add(new TermQuery(new Term("title", word)),Occur.SHOULD);
				builder.add(titleOrAbstract.build(),Occur.MUST);
			}
//			Map<String, Float> boosts = new HashMap<>();
//			boosts.put("title", 2.0f);
//			boosts.put("inventor", 2.5f);
//			boosts.put("abstract", 1.0f);
//			boosts.put("applicant", 0.9f);
//			try {
//				keyQuery = new MultiFieldQueryParser(fields, analyzer,boosts).parse(keyWords);
//				builder.add(keyQuery, Occur.MUST);
//			} catch (ParseException e1) {
//				e1.printStackTrace();
//			}
			break;			
		case TITLE:
			words=AnalyzerToken.token(keyWords,analyzer);//分词
			words=wordReplace(words,field);//替换错别字
			wordMap=getNearWordMap(words);//生成近义词

			for(String word:words) {//在title中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				BooleanQuery.Builder b=new BooleanQuery.Builder();
				b.add(new FunctionScoreQuery(new TermQuery(new Term("title", word)),new MyDoubleValuesSource(1)),Occur.SHOULD);//添加原词query查询，关系为或
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("title", w.name));
					FunctionScoreQuery f=new FunctionScoreQuery(query,new MyDoubleValuesSource(w.score));
					b.add(f, Occur.SHOULD);//添加近义词query查询，关系为或
				}
				BooleanQuery nearWordQuery=b.build();
				builder.add(nearWordQuery,Occur.MUST);//每组（原词+其近义词）查询间的关系为且
			}
			break;
		case ABSTRACT:
			words=AnalyzerToken.token(keyWords,analyzer);//分词
			words=wordReplace(words,field);//替换错别字
			wordMap=getNearWordMap(words);//生成近义词
			
			for(String word:words) {//在摘要中添加近义词查询
				List<WordEntry> s=wordMap.get(word);
				BooleanQuery.Builder b=new BooleanQuery.Builder();
				b.add(new FunctionScoreQuery(new TermQuery(new Term("abstract", word)),new MyDoubleValuesSource(1)),Occur.SHOULD);//添加原词query查询，关系为或
				for(WordEntry w:s) {
					TermQuery query = new TermQuery(new Term("abstract", w.name));
					FunctionScoreQuery f=new FunctionScoreQuery(query,new MyDoubleValuesSource(w.score));
					b.add(f, Occur.SHOULD);//添加近义词query查询，关系为或
				}
				BooleanQuery nearWordQuery=b.build();
				builder.add(nearWordQuery,Occur.MUST);//每组（原词+其近义词）查询间的关系为且
			}
			break;
			
		case APPLICANT://可查多个申请人（申请人之间是或的关系），因为多数申请人名较长，所以每个申请人使用通配符查询，申请人名不用输入全。
			words=Arrays.asList(keyWords.split(" "));//分词
			words=wordReplace(words,field);//替换错别字
			
			BooleanQuery.Builder applicantBuilder=new BooleanQuery.Builder();
			for(String word:words) {
				applicantBuilder.add(new WildcardQuery(new Term("applicant", "*"+word+"*")),Occur.SHOULD);
			}
			builder.add(applicantBuilder.build(), Occur.MUST);
			break;
		case ID://根据专利号精确查询
			keyQuery=new TermQuery(new Term("application_publish_number", keyWords));
			builder.add(keyQuery, Occur.MUST);
			break;
		case INVENTOR://可查多个发明人（发明人之间是或的关系），发明人名较短不用通配符查。
			words=Arrays.asList(keyWords.split(" "));//分词
			words=wordReplace(words,field);//替换错别字

			BooleanQuery.Builder inventorBuilder=new BooleanQuery.Builder();
			for(String word:words) {
				inventorBuilder.add(new TermQuery(new Term("inventor", word)),Occur.SHOULD);
			}
			builder.add(inventorBuilder.build(), Occur.MUST);
			break;
		case ADDRESS://地址一般较长，使用通配符查询
			keyQuery=new WildcardQuery(new Term("address", "*"+keyWords+"*"));
			builder.add(keyQuery, Occur.MUST);
			break;
		}
		
        BooleanQuery booleanQuery=builder.build();
//        Expression expr = null;
//        long low=481132800000l;
//        long high=1537372800000l;
//        long range=high-low;
//		try {
//			expr = JavascriptCompiler.compile("_score * ((popularity-low)/range*0.2+0.8)+grant_status");
//		} catch (java.text.ParseException e1) {
//			e1.printStackTrace();
//		}
//        SimpleBindings bindings = new SimpleBindings();
//        bindings.add(new SortField("_score", SortField.Type.SCORE));
//        bindings.add(new SortField("popularity", SortField.Type.LONG));
//        bindings.add(new SortField("grant_status", SortField.Type.LONG));
//        bindings.add("low",DoubleValuesSource.constant(low));
//        bindings.add("high",DoubleValuesSource.constant(high));
//        bindings.add("range",DoubleValuesSource.constant(range));
//        Query query = new FunctionScoreQuery(booleanQuery,expr.getDoubleValuesSource(bindings));
//        
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
				System.out.println(scoreDocs[i].doc);
				String titleContent=doc.get("abstract");
				TokenStream tokenstream=analyzer.tokenStream(keyWords, new StringReader(titleContent));
				try {
					titleContent=highlighter.getBestFragment(tokenstream, titleContent);
					if(titleContent==null)
						titleContent=doc.get("abstract");
				} catch (InvalidTokenOffsetsException e) {
					e.printStackTrace();
				}
				
				Patent p=new Patent();
				p.setId(doc.get("id"));
				p.setPatent_Abstract(titleContent);
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
			e.printStackTrace();
		}
		return pv;
	}
	
	private List<String> wordReplace(List<String> words,FieldType field){
		//错别字替换
		WrongWordAnalyzer wwAnalyzer=WrongWordAnalyzer.DEFAULT_WRONG_WORD_ANALYZER;
		double wwThreshold=WrongWordAnalyzer.DEFAULT_THRESHOLD;
		
		List<String> wordsReplace=new ArrayList<>();
		for(String word:words) {
			switch(field) {
			case ALL:
				wordsReplace.add(wwAnalyzer.correctWord(word, wwThreshold, "name", "word"));
				break;			
			case TITLE:
			case ABSTRACT:
				wordsReplace.add(wwAnalyzer.correctWord(word, wwThreshold, "word"));
				wordsReplace.add(wwAnalyzer.correctWord(word, wwThreshold, "word"));
				break;
			case INVENTOR:
				wordsReplace.add(wwAnalyzer.correctWord(word, wwThreshold, "name"));
				break;
			case APPLICANT:
			case ID:
			case ADDRESS:
				wordsReplace.add(word);
				break;
			}
		}
		return wordsReplace;
	}
	
	private Map<String,List<WordEntry>> getNearWordMap(List<String> words){
		//近义词查询，获取近义词
		Map<String,List<WordEntry>> wordMap=new LinkedHashMap<String,List<WordEntry>>();
		for(String w:words) {
			List<WordEntry> s=null;
			s=wordHashMap.getNearWord(w);
			wordMap.put(w,s);
		}
		System.out.println(wordMap);
		return wordMap;
	}
	
	private class MyDoubleValuesSource extends DoubleValuesSource{
		
		double boost;
		public MyDoubleValuesSource(double boost) {
			this.boost=boost;
		}

		@Override
		public boolean isCacheable(LeafReaderContext ctx) {
			return false;
		}

		@Override
		public DoubleValues getValues(LeafReaderContext ctx, DoubleValues scores) throws IOException {
			// TODO Auto-generated method stub
			assert scores != null;
		    return new MyDoubleValues(scores,boost);
		}

		@Override
		public boolean needsScores() {
			return true;
		}

		@Override
		public DoubleValuesSource rewrite(IndexSearcher reader) throws IOException {
		      return this;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
		      return obj == this;
		}

		@Override
		public String toString() {
		      return "scores";
		}
		
	}
	
	private class MyDoubleValues extends DoubleValues{

		DoubleValues in;
		double missingValue;
		boolean hasValue=false;
		
		public MyDoubleValues(DoubleValues in,double missingValue) {
			this.in=in;
			this.missingValue=missingValue;
		}
		
		@Override
		public double doubleValue() throws IOException {
			// TODO Auto-generated method stub
			return in.doubleValue()*missingValue;
		}

		@Override
		public boolean advanceExact(int doc) throws IOException {
			hasValue = in.advanceExact(doc);
	        return true;
		}
		
	}
	
}
