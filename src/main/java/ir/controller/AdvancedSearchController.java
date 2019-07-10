package ir.controller;

import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ir.enumDefine.BoolOptionSymbol;
import ir.enumDefine.FieldType;
import ir.enumDefine.IsGranted;
import ir.enumDefine.PatentTypeCode;
import ir.enumDefine.SearchAccuracy;
import ir.luceneIndex.LuceneSearcher;
import ir.models.BoolExpression;
import ir.models.PatentsForView;
import ir.services.AdvancedSearchService;
import ir.util.DateUtil;
import ir.util.seg.SegmentAnalyzer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


@Controller
public class AdvancedSearchController {
	
	@Autowired
	private AdvancedSearchService advancedSearchService;
	
	@Autowired
	private SegmentAnalyzer segmentAnalyzer;

	@GetMapping("/advancedSearch")//即什么样的页面会映射到这个方法上
	@ResponseBody
	public ModelAndView advancedSearch(@RequestParam(value="expressions",required=true)String expression,
			@RequestParam(defaultValue="1",required=false) int page,//页码
			@RequestParam(value="time_from",defaultValue="NO_LIMIT",required=false) String timeFrom,//起始时间
			@RequestParam(value="time_to",defaultValue="NO_LIMIT",required=false) String timeTo,
			@RequestParam(value="is_granted",defaultValue="NO_LIMIT",required=false) String isGrantedString,
			@RequestParam(value="type_code",defaultValue="ALL",required=false) String typeCodeString) {
		
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray Jarray = parser.parse(expression).getAsJsonArray();
		ArrayList<BoolExpression> bool = new ArrayList<BoolExpression>();
		for(JsonElement obj : Jarray ){
			BoolExpression cse = gson.fromJson( obj , BoolExpression.class);
		    bool.add(cse);  
		}
		BoolExpression[] expressions=bool.toArray(new BoolExpression[bool.size()]);;
		System.out.println(bool);
		if(page<=0)
			page=1;
		try {
			timeFrom=timeFrom.replace('-', '.');
			timeTo=timeTo.replace('-', '.');
		}catch (Exception e) {
			return Error.getErrorPage("");
		}
		PatentTypeCode typeCode;
		IsGranted isGranted;
		for(int i=0;i<expressions.length;i++) {
			try {
				FieldType.valueOf(expressions[i].field);
				BoolOptionSymbol.valueOf(expressions[i].symbol);
				expressions[i].keyWords=expressions[i].keyWords.toLowerCase();
			}catch (Exception e) {
				e.printStackTrace();
				return Error.getErrorPage("高级搜索输入错误");
			}
			
		}
		try {
			isGranted=IsGranted.valueOf(isGrantedString);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("授权条件输入错误");
		}
		try {
			typeCode=PatentTypeCode.valueOf(typeCodeString);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("类别号输入错误");
		}
		
		IndexSearcher luceneIndex = LuceneSearcher.indexes.get(SearchAccuracy.FUZZY);
		if(luceneIndex==null) {
			return Error.getErrorPage("该粒度的索引未加载");
		}
		Analyzer analyzer = segmentAnalyzer.getAnalyzer(SearchAccuracy.FUZZY);
		
		PatentsForView result;
		try {
			long startTime=System.currentTimeMillis();
			result=advancedSearchService.search(expressions,page,timeFrom,timeTo, isGranted, typeCode, luceneIndex, analyzer);
			System.out.println("搜索总共花费时间"+(System.currentTimeMillis()-startTime)+"ms");
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("搜索错误");
		}
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("show2");//设置模板名称
		modelAndView.addObject("patentsForView", result);//加入返回的结果
		modelAndView.addObject("expression",expression);

		if(result.getPageWhenOutBound()==-1)
			modelAndView.addObject("page", page);
		else 
			modelAndView.addObject("page", result.getPageWhenOutBound());
		
		modelAndView.addObject("time_from", timeFrom);
		modelAndView.addObject("time_to", timeTo);
		modelAndView.addObject("number",result.getHitsNum());
		modelAndView.addObject("typeCode",typeCode);
		modelAndView.addObject("is_granted", isGrantedString);

		
		modelAndView.addObject("year_back_3",DateUtil.timeBackPush(3));
		modelAndView.addObject("year_back_5",DateUtil.timeBackPush(5));
		modelAndView.addObject("year_back_10",DateUtil.timeBackPush(10));
		modelAndView.addObject("year_now",DateUtil.timeBackPush(0));
		
		return modelAndView;
	}
}
