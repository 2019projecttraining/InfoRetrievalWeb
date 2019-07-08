package ir.util.recommend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;


public class Recommend {
	
	//获取一篇文章中的关键词及tf-idf值
    public static Map<String, Integer> oneGetTop(int id ,IndexReader reader) throws IOException{
        int Alldoc = reader.maxDoc();
        //int docId = myMap.get(id);
        Terms terms = reader.getTermVector(id,"abstract");
        TermsEnum termsEnum = terms.iterator();
        BytesRef thisTerm = null;
        Map<String, Integer> map = new HashMap<String, Integer>();
        while ((thisTerm = termsEnum.next()) != null) {
            // 词项
            String termText = thisTerm.utf8ToString();
            // 通过totalTermFreq()方法获取词项频率
            //map.put(termText, (int) termsEnum.totalTermFreq());
            int tf = (int) termsEnum.totalTermFreq();
            int df = reader.docFreq(new Term("abstract",termsEnum.term()));
            int idf = (int) Math.log(Alldoc/df);
            int score = tf*idf;
            if (map.containsKey(termText)) {
                //存在
                int val = (Integer.parseInt(String.valueOf(map.get(termText)))+Integer.parseInt(String.valueOf(map.get(termText))));
                map.put(termText, val);
            } else {
                //不存在
                map.put(termText,score);

            }
            map.put(termText,score);
        }

        // 按value排序
        List<Map.Entry<String, Integer>> sortedMap = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(sortedMap, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        getTopN(sortedMap, 5);
        return map;

    }


    public static String[] getTopN(List<Map.Entry<String, Integer>> sortedMap, int N) {
    	if(sortedMap.size()<5)
    		N=sortedMap.size();
    	String[] temp=new String[N];
    	
        for (int i = 0; i < N; i++) {
        	temp[i]=sortedMap.get(i).getKey();
        	
           // System.out.println(sortedMap.get(i).getKey() + ":" + sortedMap.get(i).getValue());
        }
        
        System.out.println(Arrays.toString(temp));
        return temp;
    }
    //获取所有结果中的相同关键词并按照tf-idf值排序
    public static String[] allGetTop(TopDocs topdocs,IndexReader reader,int topN) throws IOException {
        ScoreDoc[] scoredocs=topdocs.scoreDocs;
        //Map<String, Integer> combineResultMap = null;
        Map<String, Integer> combineResultMap = new HashMap<String, Integer>();
        Map<String, Integer> gueest = new HashMap<String, Integer>();
        Map<String, Integer> map = new HashMap<String, Integer>();
        for(int i=0;i<scoredocs.length;i++){
            int id = scoredocs[i].doc;
            combineResultMap = map;
            map = oneGetTop(id,reader);
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                if (combineResultMap.containsKey(key)) {
                    //System.out.println(key);
                    int val = map.get(key)+map.get(key);
                    gueest.put((String) key, val);
                }
            }
            //System.out.println(gueest);
        }
        List<Map.Entry<String, Integer>> sortedMap = new ArrayList<Map.Entry<String, Integer>>(gueest.entrySet());
        Collections.sort(sortedMap, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        return getTopN(sortedMap,topN);
    }
	
}
