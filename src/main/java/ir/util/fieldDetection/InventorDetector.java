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

	public InventorDetector() {
		super();
		System.out.println("------------");
        System.out.println("加载多域查询发明者探测模块依赖文件");
        System.out.println("------------");
	}

	private final static Detector detector=new Detector("inventor","field-detection-inventor-name-dict");
	
	public boolean isInventor(String word) {
		return detector.detect(word);
	}
	
}
