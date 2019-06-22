package ir.models;

import java.util.List;

public class PatentsForView {

	//传递前端页面需要的值
	private List<Patent> patents;

	public List<Patent> getPatents() {
		return patents;
	}

	public void setPatents(List<Patent> patents) {
		this.patents = patents;
	}

}
