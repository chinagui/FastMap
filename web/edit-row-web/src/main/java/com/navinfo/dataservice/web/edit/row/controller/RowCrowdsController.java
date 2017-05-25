package com.navinfo.dataservice.web.edit.row.controller;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.row.crowds.RowCrowdsControl;

import net.sf.json.JSONObject;

/** 
* @ClassName: RowCrowdsController 
* @author: zhangpengpeng 
* @date: 2017年5月22日
* @Desc: RowCrowdsController.java
*/
@Controller
public class RowCrowdsController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(RowCrowdsController.class);
	
	@RequestMapping(value = "/crowds/token")
	public ModelAndView token(HttpServletRequest request) throws Exception {

		String parameter = request.getParameter("parameter");
		
		try {
			
			JSONObject req = JSONObject.fromObject(parameter);
			
			long userId = req.getLong("userId");
			
			AccessToken access_token = AccessTokenFactory.generate(userId);
			
			String token = access_token.getTokenString();

			return new ModelAndView("jsonView", success(token));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/crowds/csduplicate")
	public ModelAndView csduplicate(HttpServletRequest request) throws Exception {
		
		String parameter = request.getParameter("parameter");
		
		try {
			
			JSONObject reqJson = JSONObject.fromObject(parameter);
			
			RowCrowdsControl crowds = new RowCrowdsControl();
			
			int result = crowds.checkDuplicate(reqJson);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * 众包审核提交通过数据入大区域日库
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/crowds/release")
	public ModelAndView release(HttpServletRequest request) throws Exception {
		
		String parameter = request.getParameter("parameter");
		if (StringUtils.isEmpty(parameter)){
			logger.error("parameter error:没有解析到parameter参数");
			return new ModelAndView("jsonView", fail("parameter error:没有解析到parameter参数"));
		}
		try {
			
			JSONObject reqJson = JSONObject.fromObject(parameter);
			if(reqJson == null){
				logger.error("参数data数据错误！！");
				return new ModelAndView("jsonView", fail("参数data数据错误！！"));
			}
			
			RowCrowdsControl crowds = new RowCrowdsControl();
			
			String msg = crowds.release(reqJson);
			
			return new ModelAndView("jsonView", success(msg));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	public static void main(String[] argv){
		long userId = 1086L;
		
		AccessToken access_token = AccessTokenFactory.generate(userId);
		
		String token = access_token.getTokenString();
		
		System.out.println(token);
	}
}
