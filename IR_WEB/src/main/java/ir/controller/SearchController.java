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
	
	@RequestMapping("映射页面地址")//设置映射的地址，即什么样的url请求会使用这个方法返回
	public ModelAndView searchWithKeyWords(String keyWords) {
		PatentsForView result=searchService.search(keyWords);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("模板名称");//设置返回的模板名称
		modelAndView.addObject("PatentsForView", result);//返回结果对象
		return modelAndView;
	}
}
