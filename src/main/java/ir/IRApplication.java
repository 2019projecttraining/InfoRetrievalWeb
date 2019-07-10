package ir;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 全局入口
 * 
 * @author 余定邦
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IRApplication {

	public static void main(String[] args) {
		
		//启动项目后台
		SpringApplication.run(IRApplication.class, args);
		
		//自动打开浏览器
		try {
			
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://localhost:8080/");//windows
			
		} catch (IOException e) {//不是windows
			if(java.awt.Desktop.isDesktopSupported()){
	            try{
	                //创建一个URI实例,注意不是URL
	                java.net.URI uri=java.net.URI.create("http://localhost:8080/");
	                //获取当前系统桌面扩展
	                java.awt.Desktop dp=java.awt.Desktop.getDesktop();
	                //判断系统桌面是否支持要执行的功能
	                if(dp.isSupported(java.awt.Desktop.Action.BROWSE)){
	                    //获取系统默认浏览器打开链接
	                    dp.browse(uri);
	                }
	            }catch(Exception e1){
	            	System.out.println("当前系统不支持自动打开浏览器");
	            }
	        }
		}
	}
	
}
