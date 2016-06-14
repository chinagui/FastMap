package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.download.DownloadOperation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PoiController extends BaseController{
	private static final Logger logger = Logger.getLogger(EditController.class);
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/download/")
	public ModelAndView getPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray gridDateList = new JSONArray();
			gridDateList = jsonReq.getJSONArray("grid");
			
			DownloadOperation operation = new DownloadOperation();
			String url = operation.getPoiUrl(gridDateList);
			
			return new ModelAndView("jsonView", success(url));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/upload/")
	public ModelAndView importPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

//			UploadManager upload = new UploadManager();
//			
//			String filePath = upload.unzipByJobId(jobId);
//
//			Operation operation = new Operation();
//			JSONArray retArray = operation.importPoi(filePath + "/poi.txt");
			
			return new ModelAndView("jsonView", success());
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	

}
