package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.Log4jUtils;

import net.sf.json.JSONObject;

@Controller
public class PidController extends BaseController{
	private static final Logger logger = Logger.getLogger(EditController.class);
	
	/**
	 * @param request parameter={"tableName":"AU_MARK","limit":1}
	 * @param response
	 * @return 
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/pid/apply")
	public ModelAndView getPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String tableName = jsonReq.getString("tableName");
			Integer limit = jsonReq.getInt("limit");
			long startPid = PidUtil.getInstance().applyPidByTableName(tableName,limit);
			return new ModelAndView("jsonView", success(startPid));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
}
