package com.navinfo.dataservice.web.dealership.controller;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;

import net.sf.json.JSONObject;

public class DataConfirmController extends BaseController{
	private static final Logger logger = Logger.getLogger(DataPrepareController.class);
	
	@RequestMapping(value="/downInfo")
	public ModelAndView downloadInfo(HttpServletRequest request) throws Exception{
		Connection conn=null;
		try
		{
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			String chains=jsonObj.getString("chains");
			
			conn = DBConnector.getInstance().getDealershipConnection();
			
			
		}catch(Exception e){
		}
		finally{
			if(conn!=null){
				conn.close();
			}
		}
		return new ModelAndView("jsonView",fail(""));
	}
}
