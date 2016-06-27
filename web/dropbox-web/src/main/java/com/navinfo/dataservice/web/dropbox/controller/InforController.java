package com.navinfo.dataservice.web.dropbox.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.engine.dropbox.dao.DBController;

@Controller
public class InforController extends BaseController{
	private static final Logger logger = Logger.getLogger(InforController.class);
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/import/infor/")
	public ModelAndView uploadInfor(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
//			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");
			
			String filePath = getInforFilePath(jobId);
			//TODO:调用mapspoter提供的接口：将文件路径作为输入参数
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	private String getInforFilePath(int jobId) throws Exception{
		
		JSONObject uploadInfo = new DBController().getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;

		return (filePath + "/" + fileName);
		
	}
	
}
