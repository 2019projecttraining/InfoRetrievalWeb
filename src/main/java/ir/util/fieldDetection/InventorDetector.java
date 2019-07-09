package ir.util.fieldDetection;

import org.springframework.stereotype.Service;

/**
 * 姓名检测
 * 
 * 用于检测出全域查询中的发明者姓名
 * 
 * @author 余定邦
 *
 */

@Service
public class InventorDetector {

	private final static Detector detector=new Detector("inventor","field-detection-inventor-name-dict");
	
	public boolean isInventor(String word) {
		return detector.detect(word);
	}
	
}
