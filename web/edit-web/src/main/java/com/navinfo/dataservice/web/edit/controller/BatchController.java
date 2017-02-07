package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.edit.batch.BatchService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class BatchController extends BaseController {
	
	/**
	 * 批处理规则查询
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author wangdongbin
	 */
	@RequestMapping(value = "/batch/getBatchRules")
	public ModelAndView getCkRules(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		Connection conn =null;
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			int pageSize = jsonReq.getInt("pageSize");

			int pageNum = jsonReq.getInt("pageNum");
			
			int type = jsonReq.getInt("type");
			
			JSONArray result = BatchService.getInstance().getBatchRules(pageSize, pageNum, type);
			
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 执行批处理
	 * dbId	是	子任务id
	 * 根据输入的子任务和检查类型，对任务范围内的数据执行
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/batch/run")
	public ModelAndView batchRun(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);			
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			long jobId=BatchService.getInstance().batchRun(userId,jsonReq);				
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
