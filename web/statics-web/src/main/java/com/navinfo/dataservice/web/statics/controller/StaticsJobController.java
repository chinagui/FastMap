package com.navinfo.dataservice.web.statics.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;

@Controller
public class StaticsJobController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(StaticsJobController.class);
	
	@RequestMapping(value = "/staticsJob/run")
	public ModelAndView runStaticsJob(HttpServletRequest request) {
		try {
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
