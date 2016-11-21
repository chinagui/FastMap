package com.navinfo.dataservice.web.fcc.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;

/**
 * @ClassName: PretreatmentTipsController.java
 * @author y
 * @date 2016-11-16 上午11:20:04
 * @Description: 预处理tips controller
 * 
 */

@Controller
public class PretreatmentTipsController extends BaseController {

	private static final Logger logger = Logger
			.getLogger(PretreatmentTipsController.class);

	/**
	 * @Description:预处理tips创建
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/createPre")
	public ModelAndView createPretreatmentTips(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/createPre: "+parameter);
			
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			
			JSONObject tipsGeometry = jsonReq.getJSONObject("geometry");
			
			int user = jsonReq.getInt("user");

			String sourceType = jsonReq.getString("sourceType");
			
			String memo=jsonReq.getString("memo");
			
			JSONObject deep = jsonReq.getJSONObject("deep"); //tips详细信息
			
			
			if (StringUtils.isEmpty(sourceType)) {
				throw new IllegalArgumentException("参数错误：sourceType不能为空。");
			}
			
			if (tipsGeometry.isNullObject()||tipsGeometry==null) {
				throw new IllegalArgumentException("参数错误：geometry不能为空。");
			}
			
			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.create(sourceType, tipsGeometry, user,deep,memo);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	
	/**
	 * @Description:预处理tips修改--修形
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/eidtshape")
	public ModelAndView createEdgeMatchTips(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/createPre: "+parameter);
			
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			String rowkey=jsonReq.getString("rowkey");
			
			JSONObject tipsGeometry = jsonReq.getJSONObject("geometry"); //修改改坐标
			
			//String memo=jsonReq.getString("memo"); //改备注
			
			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}
			
			
			if (tipsGeometry.isNullObject()||tipsGeometry==null) {
				throw new IllegalArgumentException("参数错误：geometry不能为空。");
			}
			
			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.editGeo(rowkey, tipsGeometry, user);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	
	/**
	 * @Description:预处理tips-打断
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @author: y
	 * @time:2016-11-15 上午9:50:32
	 */
	@RequestMapping(value = "/tip/cutLine")
	public ModelAndView cutLine(HttpServletRequest request)
			throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try {
			logger.info("/tip/cutLine: "+parameter);
			
			if (StringUtils.isEmpty(parameter)) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			String rowkey=jsonReq.getString("rowkey");
			
			JSONObject pointGeo = jsonReq.getJSONObject("pointGeo"); //修改改坐标(点几何)
			
			int user = jsonReq.getInt("user");

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}
			
			if (pointGeo.isNullObject()||pointGeo==null) {
				throw new IllegalArgumentException("参数错误：pointGeo不能为空。");
			}
			
			PretreatmentTipsOperator op = new PretreatmentTipsOperator();

			op.breakLine(rowkey, pointGeo, user);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}



	
	

}
