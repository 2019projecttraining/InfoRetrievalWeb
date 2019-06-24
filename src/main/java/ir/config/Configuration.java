package ir.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 读取全局系统设置，并提供查询
 * 
 * @author 余定邦
 *
 */
public class Configuration {
	
	private final static Map<String,String> config;
	
	static {
		config=new HashMap<>();
		readInfoRetrievalWebProperties();
	}

	public static String getConfig(String key) {
		return config.get(key);
	}
	
	private static void readInfoRetrievalWebProperties() {
		try {
			URL fileUrl = Configuration.class.getClassLoader().getResource("ir_web.properties");
			
			if(fileUrl==null) {
				System.err.println("警告！配置文件不存在");
				throw new FileNotFoundException("ir_web.properties");
			}
			
			File file=new File(fileUrl.toURI());
			
			Properties prop = new Properties();   
			
            InputStream in = new BufferedInputStream (new FileInputStream(file));
            prop.load(in);     ///加载属性列表
            Iterator<String> it=prop.stringPropertyNames().iterator();
            while(it.hasNext()){
                String key=it.next();
                config.put(key, prop.getProperty(key));
                System.out.println("加载配置 \'"+key+"\' = "+prop.getProperty(key));
            }
            in.close();
            
            ///保存属性到b.properties文件
//	            FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
//	            prop.setProperty("phone", "10086");
//	            prop.store(oFile, "The New properties file");
//	            oFile.close();
		}catch (Exception e) {
			System.err.println("警告！配置文件读取失败");
			throw new RuntimeException(e);
		}
	}
	
}
