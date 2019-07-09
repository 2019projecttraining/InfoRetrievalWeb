package ir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import ir.util.ssc_fix.Ssc_Similarity;
import ir.util.ssc_fix.WrongWordAnalyzer;

/**
 * 全局入口
 * 
 * @author 余定邦
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IRApplication {

	public static void main(String[] args) {
		SpringApplication.run(IRApplication.class, args);
		IRApplication.init();
	}
	
	public static void init() {
		WrongWordAnalyzer.init();
		Ssc_Similarity.init();
	}
	
}
