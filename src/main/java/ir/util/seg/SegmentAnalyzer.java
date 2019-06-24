package ir.util.seg;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.seg.Segment;

import ir.config.Configuration;

/**
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
 */
public class SegmentAnalyzer {

	public final static Analyzer anaylzer;
	
	private final static String CONFIG_KEY="segment-analyzer";
	
	public final static String ANALYZER_NAME=Configuration.getConfig(CONFIG_KEY);
	
	static {
		anaylzer=configAnalyzer();
	}
	
	private static Analyzer hanlpAnalyzer() throws IOException {
		Segment segment=new CRFLexicalAnalyzer();
		Analyzer analyzer=new HanLPWrapperAnalyzer(segment,StopWordsLoader.load());
		return analyzer;
	}
	
	private static Analyzer configAnalyzer() {
		try {
			try {
				if(ANALYZER_NAME==null) {
					return hanlpAnalyzer();
				}
			}catch (Exception e) {
				return new SmartChineseAnalyzer();
			}
			switch(ANALYZER_NAME.toLowerCase()) {
				case "hanlp":
					return hanlpAnalyzer();
					
				case "standard":
					return new StandardAnalyzer();
					
				case "ik":
					return new IKAnalyzer();
					
				case "mmseg4j":
					return new ComplexAnalyzer();
					
				case "smart-chinese":
				default:
					return new SmartChineseAnalyzer();
				
			}
		}catch (Exception e) {
			System.err.println("警告！加载指定分词器失败,使用SmartChineseAnalyzer");
			return new SmartChineseAnalyzer();
		}
	}
	
	
}
