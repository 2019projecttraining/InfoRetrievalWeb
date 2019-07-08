package ir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import ir.util.fieldDetection.ApplicantDetection;
import ir.util.fieldDetection.ApplicationPublishNumberDetection;
import ir.util.fieldDetection.InventorDetection;
import ir.util.ssc_fix.Ssc_Similarity;
import ir.util.ssc_fix.WrongWordAnalyzer;
import ir.util.w2v.WordHashMap;

/**
 * 全局入口
 * 
 * @author 余定邦
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IRApplication {

	public static void main(String[] args) {
		SpringApplication.run(IRApplication.class, args);
	}
	
	/**
	 * 初始化加载模块数据
	 */
	public static void init() {
		WordHashMap.init();
		
		WrongWordAnalyzer.init();
		Ssc_Similarity.init();
		
		ApplicantDetection.init();
		ApplicationPublishNumberDetection.init();
		InventorDetection.init();
	}
	
}
