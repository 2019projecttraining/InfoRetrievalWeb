package ir.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller

public class HttpController {
	@GetMapping("/jump")//即什么样的页面会映射到这个方法上
	@ResponseBody
	public ModelAndView getPatentDetail() {
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("ad-search");//设置模板名称
		return modelAndView;
	}
}
