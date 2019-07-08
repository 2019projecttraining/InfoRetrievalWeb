package ir.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ir.models.Patent;
import ir.services.PatentService;

@Controller
public class detailController {

	@Autowired
	private PatentService patentService;
	
	@GetMapping("/detailSearch")//即什么样的页面会映射到这个方法上
	@ResponseBody
	public ModelAndView getPatentDetail(@RequestParam String patentId) {
		Patent result=patentService.getPatentDetail(patentId);
		System.out.println(result);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.addObject("PatentDetail", result);//加入返回的结果
		modelAndView.setViewName("lw-article-fullwidth");//设置模板名称
		return modelAndView;
	}
}
