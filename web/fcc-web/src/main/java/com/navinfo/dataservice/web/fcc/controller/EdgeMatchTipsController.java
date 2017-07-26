package com.navinfo.dataservice.web.fcc.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.fcc.tips.BaseTipsOperate;
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
			
			JSONObject jsonReq = TipsUtils.stringToSFJson(parameter);
			
			String validataMsg  = validatePars(jsonReq,"g_location","content","user","qSubTaskId");
			
			if(validataMsg!=null){
				
				throw new IllegalArgumentException("参数错误："+validataMsg+" not found");
			}

			JSONObject g_location = jsonReq.getJSONObject("g_location");

			JSONArray content=jsonReq.getJSONArray ("content");

			int user = jsonReq.getInt("user");

			String memo=null;
			
			if(jsonReq.containsKey("memo")){
				
				memo=jsonReq.getString("memo");
			}

			
			if (content==null||content.isEmpty()) {
				throw new IllegalArgumentException("参数错误：content不能为空。");
			}

            int qSubTaskId = jsonReq.getInt("qSubTaskId");
			
			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			String rowkey = op.create(g_location, content.toString(), user, memo, qSubTaskId);
			
			JSONObject  data=new JSONObject();
			
			data.put("rowkey", rowkey);

			return new ModelAndView("jsonView", success(data));

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

			BaseTipsOperate op = new BaseTipsOperate();
			
			op.updateFeedbackMemo(rowkey, user,memo);

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
			
			int delType = BaseTipsOperate.TIP_PHYSICAL_DELETE; //默认物理删除。0：逻辑删除；1：物理删除
			
			if(jsonReq.containsKey("delType")){
				
				delType = jsonReq.getInt("delType");
				
				if (delType != BaseTipsOperate.TIP_LOGICAL_DELETE && delType != BaseTipsOperate.TIP_PHYSICAL_DELETE) {
					throw new IllegalArgumentException("参数错误：delType不在范围内【0,1】");
				}
				
			}
			//逻辑删除，需要给用户,20170720取消用户
//			if(delType==0){
//
//				user = jsonReq.getInt("user");
//			}

			if (StringUtils.isEmpty(rowkey)) {
				throw new IllegalArgumentException("参数错误：rowkey不能为空。");
			}

			EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

			op.deleteByRowkey(rowkey, delType);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * 参数的验证
	 * 
	 * @param response
	 * @return
	 */
	public String validatePars(JSONObject jsonReq, String... para) {
		String notExistsKey = null;
		for (int i = 0; i < para.length; i++) {
			 if(!jsonReq.containsKey(para[i])){
				 notExistsKey=para[i];
				 break;
			}
		}
		return notExistsKey;
	}

}
