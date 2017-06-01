package com.navinfo.dataservice.web.dealership.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;

import net.sf.json.JSONObject;

/**
 * 代理店业务类
 * @author jch
 *
 */
@Controller
public class DataEditController extends BaseController {
	private static final Logger logger = Logger.getLogger(DataEditController.class);
	
	
}
