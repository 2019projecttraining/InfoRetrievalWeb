package ir.controller;

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
import ir.util.DateUtil;
import ir.util.seg.SegmentAnalyzer;
import ir.util.sscFix.SscSimilarity;
import ir.util.sscFix.WrongWordAnalyzer;
/**
 * web控制器
 * 
 * @author 余定邦
 */
@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;
	
	@Autowired
	private SegmentAnalyzer segmentAnalyzer;
	
	public SearchController() {
		super();
		WrongWordAnalyzer.init();
		SscSimilarity.init();
	}

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
			e.printStackTrace();
			return Error.getErrorPage("文件域条件输入错误");
		}
		
		try {
			letter=FirstLetterOfNamePinyin.valueOf(firstLetterOfNamePinyin);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("拼音首字母条件输入错误");
		}	
		
		try {
			timeFrom=timeFrom.replace('-', '.');
			timeTo=timeTo.replace('-', '.');
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("时间输入错误");
		}
		
		try {
			isGranted=IsGranted.valueOf(isGrantedString);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("授权条件输入错误");
		}
		
		try {
			sortedType=SortedType.valueOf(sortedTypeString);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("排序类别输入错误");
		}
		
		try {
			searchAccuracy=SearchAccuracy.valueOf(searchAccurancy);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("搜索索引粒度输入错误");
		}
		
		try {
			typeCode=PatentTypeCode.valueOf(typeCodeString);
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("类别号输入错误");
		}
		
		IndexSearcher luceneIndex = LuceneSearcher.indexes.get(searchAccuracy);
		
		if(luceneIndex==null) {
			return Error.getErrorPage("该粒度索引未加载错误");
		}
		
		Analyzer analyzer = segmentAnalyzer.getAnalyzer(searchAccuracy);
			
		//PatentsForView result;
		PatentsForView result;
		try {
			long startTime=System.currentTimeMillis();
			System.out.println("------------");
			System.out.println("进入搜索 keyWord="+keyWords);
			System.out.println("------------");
			result=searchService.search(field,keyWords, page, letter, timeFrom, timeTo, isGranted, sortedType, 
					typeCode, luceneIndex, analyzer ,searchAccuracy);
			System.out.println("搜索总共花费时间"+(System.currentTimeMillis()-startTime)+"ms");
			System.out.println();
		}catch (Exception e) {
			e.printStackTrace();
			return Error.getErrorPage("搜索错误");
		}
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("show");//设置模板名称
		modelAndView.addObject("patentsForView", result);//加入返回的结果
		
		if(result.getPageWhenOutBound()==-1)
			modelAndView.addObject("page", page);
		else 
			modelAndView.addObject("page", result.getPageWhenOutBound());
		
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
		
		modelAndView.addObject("year_back_3",DateUtil.timeBackPush(3));
		modelAndView.addObject("year_back_5",DateUtil.timeBackPush(5));
		modelAndView.addObject("year_back_10",DateUtil.timeBackPush(10));
		modelAndView.addObject("year_now",DateUtil.timeBackPush(0));
		
		return modelAndView;
	}
	
}
