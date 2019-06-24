package ir.util;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import com.hankcs.hanlp.seg.Segment;
import com.hankcs.lucene.HanLPTokenizer;

/**
 * 包装一下hanlp的分词器
 * 
 * @author 余定邦
 */
public class HanLPWrapperAnalyzer extends Analyzer{

	private Segment segmant;
    private Set<String> filter;

    public HanLPWrapperAnalyzer(Segment segmant,Set<String> filter)
    {
    	this.segmant=segmant;
        this.filter = filter;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName)
    {
        Tokenizer tokenizer = new HanLPTokenizer(segmant.enableOffset(true), filter, false);
        return new TokenStreamComponents(tokenizer);
    }
	
}
