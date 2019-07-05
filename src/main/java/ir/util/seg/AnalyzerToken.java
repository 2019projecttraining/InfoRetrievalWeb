package ir.util.seg;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * 使用lucene的Analyzer接口进行分词
 * 
 * @author 余定邦
 * @see http://lucene.apache.org/core/8_1_1/core/org/apache/lucene/analysis/package-summary.html#package.description
 *
 */
public class AnalyzerToken {

	public static List<String> token(String text, Analyzer analyzer){
		List<String> list=new ArrayList<>();
		
	     TokenStream ts = analyzer.tokenStream("", new StringReader(text));
	     // The Analyzer class will construct the Tokenizer, TokenFilter(s), and CharFilter(s),
	     //   and pass the resulting Reader to the Tokenizer.
	     try {
	       ts.reset(); // Resets this stream to the beginning. (Required)
	       while (ts.incrementToken()) {
	         // Use AttributeSource.reflectAsString(boolean)
	         // for token stream debugging.
	         list.add(ts.getAttribute(CharTermAttribute.class).toString());
	       }
	       ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
	     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	       try {
			ts.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Release resources associated with this stream.
	     }
	     
	     return list;
	}
	
}
