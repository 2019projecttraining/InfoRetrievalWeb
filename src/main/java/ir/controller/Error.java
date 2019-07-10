package ir.controller;

import org.springframework.web.servlet.ModelAndView;

public class Error {

	public static ModelAndView getErrorPage(String reason) {
		ModelAndView error=new ModelAndView();
		error.setViewName("error");
		error.addObject("reason", reason);
		return error;
	}
	
}
