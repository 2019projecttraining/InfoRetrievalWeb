package ir.models;

import java.util.List;
/**
 * 传递前端页面需要的值
 * 
 * @author 杨涛
 *
 */
public class PatentsForView {

	//传递前端页面需要的值
	private List<Patent> patents;

	private String hitsNum;
	
	private List<String> recommendWord;
	
	private int pageWhenOutBound=-1;

	public String getHitsNum() {
		return hitsNum;
	}

	public void setHitsNum(String hitsNum) {
		this.hitsNum = hitsNum;
	}
	public List<Patent> getPatents() {
		return patents;
	}

	public void setPatents(List<Patent> patents) {
		this.patents = patents;
	}

	public List<String> getRecommendWord() {
		return recommendWord;
	}

	public void setRecommendWord(List<String> recommendWord) {
		this.recommendWord = recommendWord;
	}

	public int getPageWhenOutBound() {
		return pageWhenOutBound;
	}

	public void setPageWhenOutBound(int pageWhenOutBound) {
		this.pageWhenOutBound = pageWhenOutBound;
	}

}
