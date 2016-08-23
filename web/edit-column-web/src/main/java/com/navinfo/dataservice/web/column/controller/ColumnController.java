package com.navinfo.dataservice.web.column.controller;

import java.io.IOException;

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
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * POI精编作业
 * @author wangdongbin
 *
 */
@Controller
public class ColumnController extends BaseController {
	private static final Logger logger = Logger.getLogger(ColumnController.class);
	
	/**
	 * 精编作业数据申请接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/applyPoi")
	public ModelAndView applyPoi(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();

			String firstWorkItem = jsonReq.getString("firstWorkItem");

			int groupId = jsonReq.getInt("groupId");


			ColumnCoreControl control = new ColumnCoreControl();

			control.applyData(groupId, firstWorkItem, userId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 作业数据查询接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/columnQuery")
	public ModelAndView columnQuery(HttpServletRequest request)
			throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();
			
			ColumnCoreControl control = new ColumnCoreControl();
			
			JSONArray data = control.columnQuery(userId, jsonReq);
			
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 精编保存接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/columnSave")
	public ModelAndView columnSave(HttpServletRequest request)
			throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			
			int taskId = dataJson.getInt("taskId");
			JSONArray data = dataJson.getJSONArray("data");
			String secondWorkItem = dataJson.getString("secondWorkItem");
			
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("taskId", taskId);
			jobDataJson.put("userId", userId);
			jobDataJson.put("data", data);
			jobDataJson.put("secondWorkItem", secondWorkItem);
			
			long jobId=jobApi.createJob("columnSaveJob", jobDataJson, userId, "精编保存");
			
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 精编提交接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/columnSubmit")
	public ModelAndView columnSubmit(HttpServletRequest request)
			throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			
			int taskId = dataJson.getInt("taskId");
			String firstWorkItem = dataJson.getString("firstWorkItem");
			String secondWorkItem = dataJson.getString("secondWorkItem");
			
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("taskId", taskId);
			jobDataJson.put("userId", userId);
			jobDataJson.put("firstWorkItem", firstWorkItem);
			jobDataJson.put("secondWorkItem", secondWorkItem);
			
			long jobId=jobApi.createJob("columnSubmitJob", jobDataJson, userId, "精编提交");
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
	
			logger.error(e.getMessage(), e);
	
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 查询二级作业项的统计信息
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/secondWorkStatistics")
	public ModelAndView secondWorkStatistics(HttpServletRequest request)
			throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(URLDecode(parameter));
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			
			ColumnCoreControl control = new ColumnCoreControl();
			JSONObject result = control.secondWorkStatistics(jsonReq, userId);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
	
			logger.error(e.getMessage(), e);
	
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
