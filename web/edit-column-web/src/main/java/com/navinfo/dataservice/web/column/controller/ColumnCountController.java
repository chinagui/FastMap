package com.navinfo.dataservice.web.column.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.control.column.core.ColumnCountCoreControl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: ColumnCountController
 * @Package: com.navinfo.dataservice.web.column.controller
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年11月7日
 * @Version: V1.0
 */
@Controller
public class ColumnCountController extends BaseController {

	private static final Logger logger = Logger.getLogger(ColumnCountController.class);
	private ColumnCountCoreControl columnCountCore = ColumnCountCoreControl.getInstance();

	/**
	 * 进入子任务后 - 查看当前子任务统计
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/count/queryCurrentSubtask")
	public ModelAndView queryCurrentSubtask(HttpServletRequest request) throws ServletException, IOException {

		try {
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int subtaskId = dataJson.getInt("subtaskId");

			JSONArray data = columnCountCore.queryCurrentSubtask(subtaskId);
			return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查看当前子任务统计失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}

	/**
	 * 子任务列表 - 子任务统计
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/count/querySubtask")
	public ModelAndView querySubtask(HttpServletRequest request) throws ServletException, IOException {

		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();

			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONArray subtaskIds = dataJson.getJSONArray("subtaskIds");
			if (subtaskIds.size() < 1) {
				throw new Exception("subtaskIds参数不能为空。");
			}
			
			JSONArray data = columnCountCore.querySubtask(userId, subtaskIds);
			return new ModelAndView("jsonView", success(data));

		} catch (Exception e) {
			logger.error("子任务列表 - 子任务统计失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
}
