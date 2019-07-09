package ir.util.ssc_fix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ir.config.Configuration;

/**
 * 使用音形码计算字词相似度，比传统的编辑距离好不少
 * 
 * @author 余定邦
 *
 */
public class Ssc_Similarity {

	private static Map<Character,String> sscCodeDict;
	
	private final static String sscCodeDictPath=Configuration.getConfig("ssc-code-file-path");
	
	static {
		long a=System.currentTimeMillis();
		
		loadsscCodeDict(sscCodeDictPath);
		
		System.out.println("time："+(System.currentTimeMillis()-a));
		
		System.out.println("加载完成");
	}
	
	public static void init() {}
	
	private static void loadsscCodeDict(String path) {
		sscCodeDict=new HashMap<>(90000);
		
		File f=new File(path);
		Scanner scan;
		try {
			scan = new Scanner(new FileInputStream(f),"utf-8");
			while(scan.hasNextLine()) {
				String line=scan.nextLine();
				String[] sp=line.split(" ");
				sscCodeDict.put(sp[0].charAt(0), sp[1]);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取两个字的相似程度
	 * 
	 * @param character1
	 * @param character2
	 * @return character1和character2的相似程度
	 */
	public static double[] sscCodeSim(char character1,char character2) {
		String code1=sscCodeDict.get(character1);
		String code2=sscCodeDict.get(character2);
		
		if(code1==null||code2==null)
			return new double[] {0,0};
		
		//字音
		int yunmu=difChar(code1,code2,0);//声母
		int shengmu=difChar(code1,code2,1);//韵母
		int yunmubuma=difChar(code1,code2,2);//韵母补码
		int shengdiao=difChar(code1,code2,3);//声调
		
		//字形
		int jiegou=difChar(code1,code2,4);//结构
		
		//四角编码
		int sijiao1=difChar(code1,code2,5);
		int sijiao2=difChar(code1,code2,6);
		int sijiao3=difChar(code1,code2,7);
		int sijiao4=difChar(code1,code2,8);
		//int sijiao5=difChar(code1,code2,9);//这个位置和字的形状没有关系，因此不使用
		
		//笔画数
		int bihua1=getbihua(code1);
		int bihua2=getbihua(code2);
		
		/**
		 * 字音中声母和韵母各占37.5%，辅韵母和声调各占12.5%
		 * 
		 * 字形中结构占30%，四角编码占40%，笔画占30%
		 */
		
		double ziyinSim=yunmu*0.375+shengmu*0.375+yunmubuma*0.125+shengdiao*0.125;
		double zixinSim=jiegou*0.3+0.1*(sijiao1+sijiao2+sijiao3+sijiao4)+(1-(Math.abs(bihua1-bihua2)*1.0/Math.max(bihua1, bihua2)))*0.3;
		
		return new double[] {ziyinSim,zixinSim};
	}
	
	/**
	 * 获取音形码中的笔画
	 * 
	 * @param code
	 * @return
	 */
	private static int getbihua(String code) {
		Character c=code.charAt(10);
		
		if(c>='0'&&c<='9')
			return c-'0';
		else
			return c-'A'+10;
	}
	
	/**
	 * 音形码的各位比较
	 * 
	 * @param str1
	 * @param str2
	 * @param index
	 * @return
	 */
	private static int difChar(String str1,String str2,int index) {
		return str1.charAt(index)==str2.charAt(index)?1:0;
	}
	
	/**
	 * 获取两个词的相似度
	 * 
	 * 相似度评价是将所有字的相似度相加除以字的总数
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double getSimilarity(String str1,String str2) {
		
		if(str1.length()!=str2.length())
			return 0;
		
		double zixinsim=0;
		double ziyinsim=0;
		
		int len=str1.length();
		
		for(int i=0;i<len;++i) {
			double pointziyin,pointzixin;
			if(str1.charAt(i)==str2.charAt(i)) {
				pointziyin=1;
				pointzixin=1;
			}else {
				double[] value=sscCodeSim(str1.charAt(i), str2.charAt(i));
				pointziyin=value[0];
				pointzixin=value[1];
			}
			
			zixinsim+=pointziyin/len;
			ziyinsim+=pointzixin/len;
		}
		
		return Math.max(zixinsim, ziyinsim);
	}
	
	
}
