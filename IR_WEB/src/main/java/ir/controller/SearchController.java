package ir.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ir.models.PatentsForView;
import ir.services.SearchService;
/**
 * 
 * web控制器
 * 
 * @author 余定邦
 */
@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;
	
	/**
	 *  普通页面
	 * @param keyWords
	 * @return
	 */
	@GetMapping("设置映射目标名称")//即什么样的页面会映射到这个方法上
	public ModelAndView searchWithKeyWords(@RequestParam String keyWords,
			@RequestParam(value="pinyin",required=false) String firstLetterOfNamePinyin,
			@RequestParam(value="time_from",defaultValue="NO_VALUE",required=false) String timeFrom,
			@RequestParam(value="time_to",defaultValue="NO_VALUE",required=false) String timeTo,
			@RequestParam(value="time_order",defaultValue="DESC",required=false) String timeOrder,
			@RequestParam(value="is_granted",defaultValue="NO_LIMIT",required=false) String isGranted,
			@RequestParam(value="sort_type",defaultValue="COMPREHENSIVE",required=false) String sortedType) {
		
		
		
		
		
		
		PatentsForView result=searchService.search(keyWords);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置模板名称
		modelAndView.addObject("PatentsForView", result);//加入返回的结果
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
		PatentsForView result=searchService.search(patentId);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置模板名称
		modelAndView.addObject("Patents", result);//加入返回的结果
		return modelAndView;
	}
}
