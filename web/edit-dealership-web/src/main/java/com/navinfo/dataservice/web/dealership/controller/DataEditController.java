package com.navinfo.dataservice.web.dealership.controller;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.dealership.service.DataEditService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 代理店业务类
 * 
 * @author jch
 *
 */
@Controller
public class DataEditController extends BaseController {
	private static final Logger logger = Logger.getLogger(DataEditController.class);

	private DataEditService dealerShipEditService = DataEditService.getInstance();

	@RequestMapping(value = "/applyData")
	public ModelAndView applyData(HttpServletRequest request) throws Exception {
		Connection conn = null;

		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");

			AccessToken tokenObj = (AccessToken) request.getAttribute("access_token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			conn = DBConnector.getInstance().getConnectionById(399);

			int data = dealerShipEditService.applyDataService(chainCode, conn, userId);
			Map<String, Integer> result = new HashMap<>();
			result.put("data", data);

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				conn.close();
			}
		} // finally
	}
	
	//代理店启动作业接口
	@RequestMapping(value = "/startWork")
	public ModelAndView queryDealerBrand(HttpServletRequest request) {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("access_token");
      		JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();
			String chainCode = dataJson.getString("chainCode");
			String msg = dealerShipEditService.startWork(chainCode, userId);
			
			return new ModelAndView("jsonView", success(msg));
		} catch (Exception e) {
			logger.error("启动录入作业失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
  
	@RequestMapping(value = "/loadWorkList")
	public ModelAndView loadWorkList(HttpServletRequest request) throws Exception {
		Connection conn = null;

		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
			int dealStatus = dataJson.getInt("dealSatus");


			AccessToken tokenObj = (AccessToken) request.getAttribute("access_token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			conn = DBConnector.getInstance().getConnectionById(399);

			// TODO具体逻辑
			//这里引用的jar要是一个版本的，否则更细心代码下来都编译报错了 modify:songhe
			JSONArray data = dealerShipEditService.startWorkService(chainCode, conn, userId, dealStatus);
			Map<String, JSONArray> result = new HashMap<>();

			result.put("data", data);

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				conn.close();
			}
		} 
	}
      

	
	//代理店清除关联poi接口
	@RequestMapping(value = "/clearRelatedPoi")
	public ModelAndView clearRelatedPoi(HttpServletRequest request) {
		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int resultId = dataJson.getInt("resultId");
			dealerShipEditService.clearRelatedPoi(resultId);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} 
	}
  
  @RequestMapping(value = "/saveData")
	public ModelAndView saveData(HttpServletRequest request) throws Exception {
		Connection conn = null;

		try {
			JSONObject parameter = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (parameter == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			AccessToken tokenObj = (AccessToken) request.getAttribute("access_token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			String data = dealerShipEditService.saveDataService(parameter,userId);
			Map<String, String> result = new HashMap<>();
			result.put("data", data);

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
		@RequestMapping(value = "/commitDealership")
	public ModelAndView commitDealership(HttpServletRequest request) throws Exception {
		Connection conn = null;

		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");
		
			conn = DBConnector.getInstance().getDealershipConnection();
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("access_token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			dealerShipEditService.commitDealership(chainCode,conn,userId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				conn.close();
			}
		} // finally
	}
	
	@RequestMapping(value = "/diffDetail")
	public ModelAndView diffDetail(HttpServletRequest request) throws Exception{
		Connection conn = null;
		
		try{
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			
			int resultId=jsonObj.getInt("resultId");
			conn = DBConnector.getInstance().getDealershipConnection();
			
			JSONObject data = dealerShipEditService.diffDetailService(resultId, conn);
			Map<String, Object> result = new HashMap<>();
			result.put("data", data);

			return new ModelAndView("jsonView", success(result));
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		finally{
			if (conn != null) {
				conn.close();
			}
		}//
	}
}
