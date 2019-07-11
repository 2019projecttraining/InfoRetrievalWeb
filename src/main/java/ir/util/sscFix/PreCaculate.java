package ir.util.sscFix;
//package ir.util.ssc_fix;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//
//public class PreCaculate {
//	
//	static List<Character> zis = new ArrayList<>();
//	
//	static {
//		loadDict("C:\\Users\\HPuser\\Desktop\\common_char.txt");
//	}
//
//	public static void main(String[] arg) throws UnsupportedEncodingException, IOException {
//		
//		System.out.println(zis.size());
//		
//		File fout=new File("D:\\data\\ssc_pre_cul.txt");
//		FileOutputStream fops=new FileOutputStream(fout);
//
//		int count=0;
//		for (int i = 0; i < zis.size(); ++i) {
//			for (int j = i + 1; j < zis.size(); ++j) {
//				++count;
//				if(count%10000==0)
//					System.out.println((count/10000)+"w");
//				double sim = Sim.yinxinCodeSim(zis.get(i), zis.get(j));
//				fops.write((zis.get(i)+" "+zis.get(j)+" "+String.format("%.3f", sim)+"\n").getBytes("utf-8"));
//			}
//		}
//		
//		fops.close();
//	}
//	
//	private static void loadDict(String path) {
//		File f=new File(path);
//		Scanner scan;
//		try {
//			scan = new Scanner(new FileInputStream(f),"utf-8");
//			while(scan.hasNextLine()) {
//				String line=scan.nextLine();
//				for(char c:line.toCharArray()) {
//					if(!Character.isSpaceChar(c))
//						zis.add(c);
//				}
//			}
//			scan.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//}
