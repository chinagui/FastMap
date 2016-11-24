package com.navinfo.dataservice.web.fcc.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.fcc.tips.EdgeMatchTipsOperator;

/**
 * @ClassName: EdgeMatchTipsController.java
 * @author y
 * @date 2016-11-16 上午11:20:04
 * @Description: 接边标识tips controller
 * 
 */

@Controller
public class EdgeMatchTipsController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(EdgeMatchTipsController.class);

	/**
	 * @Description:接边标识tips创建
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/createEdgeMatch")
	public ModelAndView createEdgeMatchTips(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONObject g_location = jsonReq.getJSONObject("g_location");

			//JSONObject g_guide = jsonReq.getJSONObject("g_guide");
			
			//JSONArray feedback = jsonReq.getJSONArray("feedback");
			
			String content=jsonReq.getString("content");

			int user = jsonReq.getInt("user");

			String sourceType = jsonReq.getString("sourceType");
			
			String memo=jsonReq.getString("memo");
			
			//String deep = jsonReq.getString("deep");
			
			if (StringUtils.isEmpty(content)) {
				throw new IllegalArgumentException("参数错误：content不能为空。");
			}
			
			
			if (StringUtils.isEmpty(sourceType)) {
				throw new IllegalArgumentException("参数错误：sourceType不能为空。");
			}
			
			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			op.create(sourceType, g_location, content, user,memo);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	/**
	 * @Description:接边标识tips修改，改备注
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/editMemo")
	public ModelAndView editMemo(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String memo = jsonReq.getString("memo");

			String rowkey = jsonReq.getString("rowkey");
			
			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}
			

		/*	if (StringUtils.isEmpty(user)) {
				throw new IllegalArgumentException("参数错误：user不能为空。");
			}*/

			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();
			
			
			int stage=2; //接边标识和fc预处理都默认为2
			
			op.updateFeedbackMemo(rowkey, user,memo,stage);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	
	/**
	 * @Description:删除tips
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/deleteByRowkey")
	public ModelAndView deleteByRowkey(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String rowkey = jsonReq.getString("rowkey");
			

			//int user = jsonReq.getInt("user");  //???

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			op.deleteByRowkey(rowkey);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

}
