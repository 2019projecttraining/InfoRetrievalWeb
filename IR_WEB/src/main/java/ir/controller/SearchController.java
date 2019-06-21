package ir.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ir.models.PatentsForView;
import ir.services.SearchService;

@Controller
public class SearchController {

	@Autowired
	private SearchService searchService;
	
	@RequestMapping("设置映射目标名称")//即什么样的页面会映射到这个方法上
	public ModelAndView searchWithKeyWords(String keyWords) {
		PatentsForView result=searchService.search(keyWords);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置模板名称
		modelAndView.addObject("PatentsForView", result);//加入返回的结果
		return modelAndView;
	}
}
