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
	
	@RequestMapping("ӳ��ҳ���ַ")//����ӳ��ĵ�ַ����ʲô����url�����ʹ�������������
	public ModelAndView searchWithKeyWords(String keyWords) {
		PatentsForView result=searchService.search(keyWords);
		ModelAndView modelAndView=new ModelAndView();
		modelAndView.setViewName("ģ������");//���÷��ص�ģ������
		modelAndView.addObject("PatentsForView", result);//���ؽ������
		return modelAndView;
	}
}
