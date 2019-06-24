package ir.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ir.enumDefine.FirstLetterOfNamePinyin;
import ir.enumDefine.IsGranted;
import ir.enumDefine.SortedType;
import ir.models.Patent;
import ir.models.PatentsForView;
import ir.services.PatentService;
import ir.services.SearchService;
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
	private PatentService patentService;
	
	/**
	 *  普通页面
	 * @param keyWords
	 * @return
	 */
	@GetMapping("设置映射目标名称")//即什么样的页面会映射到这个方法上
	public ModelAndView searchWithKeyWords(@RequestParam String keyWords,
			@RequestParam(defaultValue="1",required=false) int page,//页码
			@RequestParam(value="pinyin",defaultValue="NO_LIMIT",required=false) String firstLetterOfNamePinyin,//拼音首字母
			@RequestParam(value="time_from",defaultValue="NO_LIMIT",required=false) String timeFrom,//起始时间
			@RequestParam(value="time_to",defaultValue="NO_LIMIT",required=false) String timeTo,//截至时间
			@RequestParam(value="is_granted",defaultValue="NO_LIMIT",required=false) String isGrantedString,//是否授权
			@RequestParam(value="sort_type",defaultValue="COMPREHENSIVE",required=false) String sortedTypeString) {//排序类型
		
		FirstLetterOfNamePinyin letter;
		IsGranted isGranted;
		SortedType sortedType;
		
		try {
			letter=FirstLetterOfNamePinyin.valueOf(firstLetterOfNamePinyin);
		}catch (Exception e) {
			// TODO: to error page
			return null;
		}	
		
		try {
			isGranted=IsGranted.valueOf(isGrantedString);
		}catch (Exception e) {
			// TODO: to error page
			return null;
		}
		
		try {
			sortedType=SortedType.valueOf(sortedTypeString);
		}catch (Exception e) {
			// TODO: to error page
			return null;
		}
			
		PatentsForView result;
		try {
			result=searchService.search(keyWords, page, letter, timeFrom, timeTo, isGranted, sortedType);
		}catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置模板名称
		modelAndView.addObject("PatentsForView", result);//加入返回的结果
		modelAndView.addObject("Page", page);
		return modelAndView;
		
	}
	
	/**
	 * 弹出的细节页面
	 * 
	 * @param patentId
	 * @return
	 */
	@GetMapping("设置映射目标名称")
	public ModelAndView getPatentDetail(@RequestParam String patentId) {
		Patent result=patentService.getPatentDetail(patentId);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置模板名称
		modelAndView.addObject("PatentDetail", result);//加入返回的结果
		return modelAndView;
	}
}
