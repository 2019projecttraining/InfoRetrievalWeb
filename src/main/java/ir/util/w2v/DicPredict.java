package ir.util.w2v;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.hankcs.hanlp.mining.word2vec.WordVectorModel;
/**
 * 此类用来加载词向量词典，对关键词预测近义词，并将预测结果写入txt
 * @author 杨涛
 *
 */
public class DicPredict {
	
	private final static String path="C:\\Users\\HPuser\\Desktop\\merge_sgns_bigram_char300.txt";
	private final static WordVectorModel wordVectorModel;
	static {
		wordVectorModel=getDic();//加载300维词向量词典
	}
	
	private static WordVectorModel getDic(){
		long start = System.currentTimeMillis();
		WordVectorModel wordVectorModel = null;
		try {
			wordVectorModel = new WordVectorModel(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("load dic time " + (System.currentTimeMillis() - start));
		return wordVectorModel;
	}
	
	public List<Map.Entry<String, Float>> predit(String s) {
		return wordVectorModel.nearest(s);
	}

	public static void main(String[] args) throws IOException {
		DicPredict t=new DicPredict();
//		while(true) {
//			Scanner scan=new Scanner(System.in);
//			t.predit(scan.nextLine());
//		}
		//获取所有待预测近义词的关键词
		FileInputStream in=new FileInputStream("C:\\Users\\HPuser\\Desktop\\关键词.txt");
		BufferedReader bf=new BufferedReader(new InputStreamReader(in,"GBK"));
		List<String> l=new ArrayList<String>();
		String temp=null;
		while((temp=bf.readLine())!=null) {
			l.add(temp);
		}
		bf.close();
		//预测的近义词结果存入txt文件
		OutputStreamWriter write =new OutputStreamWriter(new FileOutputStream("C:\\\\Users\\\\HPuser\\\\Desktop\\\\近义词.txt",true),"GBK");
		BufferedWriter bw=new BufferedWriter(write);
		int count=1;
		for(String word:l) {
			if(count<=0)
				continue;
			List<Map.Entry<String, Float>> a=t.predit(word);
			if(a.size()!=0) {
				String line=word+" "+a.toString().replace("[","").replace("]", "");
				bw.write(line);
				bw.newLine();
			}
			count++;
			if(count%100==99)
				System.out.println("当前进度："+count);
		}
		bw.close();
	}
}
