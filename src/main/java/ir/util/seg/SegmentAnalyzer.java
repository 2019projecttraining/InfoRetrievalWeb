package ir.util.seg;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.seg.Segment;
import com.huaban.analysis.jieba.JiebaSegmenter;

import ir.config.Configuration;
import ir.enumDefine.SearchAccuracy;
import ir.util.seg.jieba.JiebaAnalyzer;

/**
 * v1
 * 分词器的加载，可以选用多种分词器：
 * 目前支持5种分词器：
 * hanlp，
 * standard，
 * ik，
 * mmseg4j，
 * smart-chinese
 * 
 * 通过配置segment-analyzer来设置需要的分词器
 * 默认使用hanlp分词器
 * 错误时使用smart-chinese分词器
 * 
 * @author 余定邦
 * 
 * v2
 * 分词器改为4种粒度，
 * 第一是粗粒度分词，
 * 第二是细粒度分词，是在粗粒度的基础上继续进行细分
 * 第三是单字切分
 * 第四的双字切分
 * 
 * 对应粗粒度切分的分词器，需配置coarse-grained-segment-analyzer
 * 可配置的分词器有hanlp，hanlp-self-train-crf,jieba，ik和smart-chinese
 * 
 * 对于细粒度切分的分词器，需配置fine-grained-segment-analyzer
 * 可配置的分词器有hanlp,hanlp-self-train-crf和jieba
 * 
 * 对应单字切分的分词器，需配置single-word-segment-analyzer
 * 可配置的分词器仅有standard
 * 
 * 对应双字切分的分词器，需配置double-word-segment-analyzer
 * 可配置的分词器仅有cjk
 * 
 * 移除了v1中的mmseg4j，因为和当前lucene版本不兼容
 * 
 * @author 余定邦
 * 
 */
@SuppressWarnings("resource")
@Service
public class SegmentAnalyzer {
	
	/**
	 * 给全局提供分词器的获取，可以根据不同的分词粒度获取不同的分词器
	 */
	private final static Map<SearchAccuracy,Analyzer> analyzers;
	
	/**
	 * 获取一个粒度适合的分词器，如果这个粒度对应的分词器不存在，那么使用smart-chinese分词器
	 * 
	 * @param searchAccuracy
	 * @return
	 */
	public Analyzer getAnalyzer(SearchAccuracy searchAccuracy) {
		
		Analyzer res=analyzers.get(searchAccuracy);
		
		return res==null?DEFAULT_EXCEPTION_HANDLE_ANALYZER:res;
	}
	
	private final static CharArraySet STOP_WORDS=new CharArraySet(StopWordsLoader.stopWords, true);
	
	
	
	private final static String COARSE_GRAINED_CONFIG_KEY="coarse-grained-segment-analyzer";
	
	private final static String FINE_GRAINED_CONFIG_KEY="fine-grained-segment-analyzer";
	
	private final static String SINGLE_WORD_CONFIG_KEY="single-word-segment-analyzer";
	
	private final static String DOUBLE_WORD_CONFIG_KEY="double-word-segment-analyzer";
	
	
	
	private final static String COARSE_GRAINED_ANALYZER_NAME=Configuration.getConfig(COARSE_GRAINED_CONFIG_KEY);
	
	private final static String FINE_GRAINED_ANALYZER_NAME=Configuration.getConfig(FINE_GRAINED_CONFIG_KEY);
	
	private final static String SINGLE_WORD_ANALYZER_NAME=Configuration.getConfig(SINGLE_WORD_CONFIG_KEY);
	
	private final static String DOUBLE_WORD_ANALYZER_NAME=Configuration.getConfig(DOUBLE_WORD_CONFIG_KEY);
	
	
	
	public final static Analyzer DEFALUT_COARSE_GRAINED_ANALYZER;
	
	public final static Analyzer DEFALUT_FINE_GRAINED_ANALYZER;
	
	public final static Analyzer DEFALUT_SINGLE_WORD_GRAINED_ANALYZER;
	
	public final static Analyzer DEFALUT_DOUBLE_WORD_GRAINED_ANALYZER;
	
	private final static Analyzer DEFAULT_EXCEPTION_HANDLE_ANALYZER=new SmartChineseAnalyzer(STOP_WORDS);
	
	
	
	
	private final static String HANLP_CRF_MODEL_PATH_KEY="hanlp-crf-model-path";
	
	private final static String HANLP_CRF_MODEL_PATH=Configuration.getConfig(HANLP_CRF_MODEL_PATH_KEY);
	
	private final static String HANLP_DEFAULT_CRF_MODEL_PATH_KEY="hanlp-default-crf-model-path";
	
	private final static String HANLP_DEFAULT_CRF_MODEL_PATH=Configuration.getConfig(HANLP_DEFAULT_CRF_MODEL_PATH_KEY);

	
	static {
		Analyzer temp;
		try {
			temp=hanlpAnalyzer(HANLP_CRF_MODEL_PATH,false);
		}catch (Exception e) {
			temp=DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
		DEFALUT_COARSE_GRAINED_ANALYZER=temp;
		
		try {
			temp=hanlpAnalyzer(HANLP_CRF_MODEL_PATH,true);
		}catch (Exception e) {
			temp=DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
		DEFALUT_FINE_GRAINED_ANALYZER=temp;
		
		try {
			temp=new StandardAnalyzer(STOP_WORDS);
		}catch (Exception e) {
			temp=DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
		DEFALUT_SINGLE_WORD_GRAINED_ANALYZER=temp;
		
		try {
			temp=new CJKAnalyzer(STOP_WORDS);
		}catch (Exception e) {
			temp=DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
		DEFALUT_DOUBLE_WORD_GRAINED_ANALYZER=temp;
		
		Analyzer coarseGrainedAnaylzer=configCoarseGrainedAnalyzer();
		Analyzer fineGrainedAnalyzer=configFineGrainedAnalyzer();
		Analyzer singleWordAnalyzer=configSingleWordAnalyzer();
		Analyzer doubleWordAnalyzer=configDoubleWordAnalyzer();
		
		Map<SearchAccuracy,Analyzer> tempMap=new HashMap<>();
		
		tempMap.put(SearchAccuracy.ACCURATE, coarseGrainedAnaylzer);
		tempMap.put(SearchAccuracy.FUZZY, fineGrainedAnalyzer);
		tempMap.put(SearchAccuracy.SINGLE_WORD, singleWordAnalyzer);
		tempMap.put(SearchAccuracy.DOUBLE_WORD, doubleWordAnalyzer);
		
		analyzers=Collections.unmodifiableMap(tempMap);
	}
	
	private static Analyzer hanlpAnalyzer(String modelPath,boolean indexMode) throws IOException {
		Segment segment=new CRFLexicalAnalyzer(modelPath).enableIndexMode(indexMode);
		Analyzer analyzer=new HanLPWrapperAnalyzer(segment,StopWordsLoader.stopWords);
		return analyzer;
	}
	
	private static Analyzer configCoarseGrainedAnalyzer() {
		if(COARSE_GRAINED_ANALYZER_NAME==null) {
			return DEFALUT_COARSE_GRAINED_ANALYZER;
		}
		try {
			switch(COARSE_GRAINED_ANALYZER_NAME.toLowerCase()) {
				case "default":
				case "hanlp-self-train-crf":
					return DEFALUT_COARSE_GRAINED_ANALYZER;
					
				case "hanlp":
					return hanlpAnalyzer(HANLP_DEFAULT_CRF_MODEL_PATH,false);
					
				case "ik":
					return new IKAnalyzer(true);
					
				case "jieba":
					return new JiebaAnalyzer(JiebaSegmenter.SegMode.SEARCH,STOP_WORDS);
					
				case "smart-chinese":
				default:
					System.err.println("警告！配置中使用了未知的分词器，将使用SmartChineseAnalyzer");
					return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
			}
		}catch (Exception e) {
			System.err.println("警告！加载指定分词器失败，使用SmartChineseAnalyzer");
			return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
	}
	
	private static Analyzer configFineGrainedAnalyzer() {
		if(FINE_GRAINED_ANALYZER_NAME==null) {
			return DEFALUT_FINE_GRAINED_ANALYZER;
		}
		try {
			switch(FINE_GRAINED_ANALYZER_NAME.toLowerCase()) {
				case "default":
				case "hanlp-self-train-crf":
					return DEFALUT_FINE_GRAINED_ANALYZER;
					
				case "hanlp":
					return hanlpAnalyzer(HANLP_DEFAULT_CRF_MODEL_PATH,true);
					
				case "jieba":
					return new JiebaAnalyzer(JiebaSegmenter.SegMode.INDEX);
					
				default:
					System.err.println("警告！配置中使用了未知的分词器，将使用SmartChineseAnalyzer");
					return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
			}
		}catch (Exception e) {
			System.err.println("警告！加载指定分词器失败,使用SmartChineseAnalyzer");
			return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
	}
	
	private static Analyzer configSingleWordAnalyzer() {
		if(SINGLE_WORD_ANALYZER_NAME==null) {
			return DEFALUT_SINGLE_WORD_GRAINED_ANALYZER;
		}
		try {
			switch(SINGLE_WORD_ANALYZER_NAME.toLowerCase()) {
				case "default":
				case "standard":
					return DEFALUT_SINGLE_WORD_GRAINED_ANALYZER;
					
				default:
					System.err.println("警告！配置中使用了未知的分词器，将使用SmartChineseAnalyzer");
					return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
			}
		}catch (Exception e) {
			System.err.println("警告！加载指定分词器失败,使用SmartChineseAnalyzer");
			return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
	}
	
	private static Analyzer configDoubleWordAnalyzer() {
		if(DOUBLE_WORD_ANALYZER_NAME==null) {
			return DEFALUT_DOUBLE_WORD_GRAINED_ANALYZER;
		}
		try {
			switch(DOUBLE_WORD_ANALYZER_NAME.toLowerCase()) {
				case "default":
				case "cjk":
					return DEFALUT_DOUBLE_WORD_GRAINED_ANALYZER;
					
				default:
					System.err.println("警告！配置中使用了未知的分词器，将使用SmartChineseAnalyzer");
					return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
			}
		}catch (Exception e) {
			System.err.println("警告！加载指定分词器失败,使用SmartChineseAnalyzer");
			return DEFAULT_EXCEPTION_HANDLE_ANALYZER;
		}
	}
	
}
