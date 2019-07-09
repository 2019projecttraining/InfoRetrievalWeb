package ir.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ir.enumDefine.FieldType;
import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.PatentTypeCode;
import ir.enumDefine.SearchAccuracy;
import ir.enumDefine.SortedType;
import ir.luceneIndex.LuceneSearcher;
import ir.models.PatentsForView;
import ir.services.SearchService;
import ir.util.seg.SegmentAnalyzer;
/**
 * web控制器
 * 
 * @author 余定邦
 */
@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;
	
//	@Autowired
//	private PatentService patentService;
	
	/**
	 *  普通页面
	 * @param keyWords
	 * @return
	 */
	@GetMapping("/search")//即什么样的页面会映射到这个方法上
	@ResponseBody
	public ModelAndView searchWithKeyWords(@RequestParam(value="field",defaultValue="ALL",required=false) String fieldString,
			@RequestParam String keyWords,
			@RequestParam(defaultValue="1",required=false) int page,//页码
			@RequestParam(value="pinyin",defaultValue="NO_LIMIT",required=false) String firstLetterOfNamePinyin,//拼音首字母
			@RequestParam(value="time_from",defaultValue="NO_LIMIT",required=false) String timeFrom,//起始时间
			@RequestParam(value="time_to",defaultValue="NO_LIMIT",required=false) String timeTo,//截至时间
			@RequestParam(value="is_granted",defaultValue="NO_LIMIT",required=false) String isGrantedString,//是否授权
			@RequestParam(value="sort_type",defaultValue="COMPREHENSIVENESS",required=false) String sortedTypeString,//排序类型
			@RequestParam(value="search_accurancy",defaultValue="FUZZY",required=false) String searchAccurancy,//搜索精确度
			@RequestParam(value="type_code",defaultValue="ALL",required=false) String typeCodeString) {//根据类别筛选

		keyWords=keyWords.toLowerCase();
		
		FieldType field;
		FirstLetterOfNamePinyin letter;
		IsGranted isGranted;
		SortedType sortedType;
		SearchAccuracy searchAccuracy;
		PatentTypeCode typeCode;
		
		if(page<=0)
			page=1;
		
		try {
			field=FieldType.valueOf(fieldString);
		}catch (Exception e) {
			// TODO: to error page
			return null;
		}
		
		try {
			letter=FirstLetterOfNamePinyin.valueOf(firstLetterOfNamePinyin);
		}catch (Exception e) {
			// TODO: to error page
			return null;
		}	
		
		try {
			timeFrom=timeFrom.replace('-', '.');
			timeTo=timeTo.replace('-', '.');
		}catch (Exception e) {
			// TODO: to error page
			e.printStackTrace();
			return null;
		}
		
		try {
			isGranted=IsGranted.valueOf(isGrantedString);
		}catch (Exception e) {
			// TODO: to error page
			e.printStackTrace();
			return null;
		}
		
		try {
			sortedType=SortedType.valueOf(sortedTypeString);
		}catch (Exception e) {
			// TODO: to error page
			e.printStackTrace();
			return null;
		}
		
		try {
			searchAccuracy=SearchAccuracy.valueOf(searchAccurancy);
		}catch (Exception e) {
			// TODO: to error page
			e.printStackTrace();
			return null;
		}
		
		try {
			typeCode=PatentTypeCode.valueOf(typeCodeString);
		}catch (Exception e) {
			// TODO: to error page
			e.printStackTrace();
			return null;
		}
		
		IndexSearcher luceneIndex = LuceneSearcher.indexes.get(searchAccuracy);
		
		if(luceneIndex==null) {
			// TODO: to error page
			return null;
		}
		
		Analyzer analyzer = SegmentAnalyzer.getAnalyzer(searchAccuracy);
			
		//PatentsForView result;
		PatentsForView result;
		try {
			long startTime=System.currentTimeMillis();
			System.out.println("执行到这里");
			result=searchService.search(field,keyWords, page, letter, timeFrom, timeTo, isGranted, sortedType, 
					typeCode, luceneIndex, analyzer);
			System.out.println("搜索总共花费时间"+(System.currentTimeMillis()-startTime)+"ms");
		}catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("show");//设置模板名称
		modelAndView.addObject("patentsForView", result);//加入返回的结果
		modelAndView.addObject("page", page);
		modelAndView.addObject("pinyin", firstLetterOfNamePinyin);
		modelAndView.addObject("time_from", timeFrom);
		modelAndView.addObject("time_to", timeTo);
		modelAndView.addObject("is_granted", isGrantedString);
		modelAndView.addObject("sort_type", sortedTypeString);
		modelAndView.addObject("keyWords",keyWords);
		modelAndView.addObject("field",field);
		modelAndView.addObject("number",result.getHitsNum());
		modelAndView.addObject("search_accurancy",searchAccuracy);
		modelAndView.addObject("typeCode",typeCode);
		
		modelAndView.addObject("year_back_3",timeBackPush(3));
		modelAndView.addObject("year_back_5",timeBackPush(5));
		modelAndView.addObject("year_back_10",timeBackPush(10));
		modelAndView.addObject("year_now",timeBackPush(0));
		
//		modelAndView.addObject("patent_type_code", new PatentTypeCode[] {PatentTypeCode.A,PatentTypeCode.B,PatentTypeCode.C,
//				PatentTypeCode.D,PatentTypeCode.E,PatentTypeCode.F,PatentTypeCode.G,PatentTypeCode.H});
		
		return modelAndView;
	}
	
	public final static long YEAR_TIME=365l*24*60*60*1000;
	
	public static String timeBackPush(int yearBackPush) {
		SimpleDateFormat sdf=new SimpleDateFormat("YYYY-MM-dd");
		return sdf.format(new Date(System.currentTimeMillis()-yearBackPush*YEAR_TIME));
	}
}
