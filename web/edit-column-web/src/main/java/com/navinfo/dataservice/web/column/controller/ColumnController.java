package com.navinfo.dataservice.web.column.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;

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
	 * POI月编作业数据申请接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/column/applyPoi")
	public ModelAndView applyPoi(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();

			ColumnCoreControl control = new ColumnCoreControl();

			int count = control.applyData(jsonReq, userId);

			return new ModelAndView("jsonView", success(count));

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
			
			JSONObject data = control.columnQuery(userId, jsonReq);
			
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
			
			long jobId=jobApi.createJob("columnSaveJob", jobDataJson, userId,taskId, "精编保存");
			
			
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
			
			long jobId=jobApi.createJob("columnSubmitJob", jobDataJson, userId,taskId, "精编提交");
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
	
			logger.error(e.getMessage(), e);
	
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	/**
	 * 精编任务的统计查询
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/deep/task/statistics")
	public ModelAndView taskStatistics(HttpServletRequest request)
			throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		try {
			JSONObject jsonReq = JSONObject.fromObject(URLDecode(parameter));
			
			ColumnCoreControl control = new ColumnCoreControl();
			JSONObject result = control.taskStatistics(jsonReq);
			
			return new ModelAndView("jsonView", success(result));
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
	@RequestMapping(value = "/poi/column/secondWorkStatistics")
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
	
	/**
	 * 根据作业组，查询精编任务列表
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "poi/deep/queryTaskList")
	public ModelAndView queryTaskList(HttpServletRequest request)
			throws ServletException, IOException {
		try {
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			
			long userId=tokenObj.getUserId();
			
			ColumnCoreControl control = new ColumnCoreControl();
			List result = control.queryTaskList(userId,jsonReq);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
	
			logger.error(e.getMessage(), e);
	
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}


	
	/**
	 * 月编专项库存统计接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/column/queryKcLog")
	public ModelAndView getLogCount(HttpServletRequest request) throws ServletException, IOException {
		
		String parameter = request.getParameter("parameter");
		
		logger.debug("月编专项库存总量统计");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			logger.debug("parameter="+jsonReq);
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			
			long userId = tokenObj.getUserId();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ColumnCoreControl columnControl = new ColumnCoreControl();
			
			JSONObject result = columnControl.getLogCount(subtaskId, userId);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
