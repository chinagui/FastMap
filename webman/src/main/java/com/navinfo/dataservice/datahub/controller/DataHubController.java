package com.navinfo.dataservice.datahub.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dms.commons.springmvc.BaseController;

/** 
 * @ClassName: DataHubController 
 * @author Xiao Xiaowen 
 * @date 2015-11-27 上午11:44:30 
 * @Description: TODO
 */
@Controller
public class DataHubController extends BaseController {
	@RequestMapping(value = "/datahub/getdb/")
	public ModelAndView getDb(HttpServletRequest request){
		return new ModelAndView("jsonView", "data", "Hello,Datahub.");
	}
}
