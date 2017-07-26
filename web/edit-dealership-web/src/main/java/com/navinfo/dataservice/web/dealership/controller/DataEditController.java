package com.navinfo.dataservice.web.dealership.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.dealership.service.DataEditService;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

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

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			conn = DBConnector.getInstance().getDealershipConnection();

			int data = dealerShipEditService.applyDataService(chainCode, conn, userId);

			return new ModelAndView("jsonView", success(data));

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
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
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
			int dealStatus = dataJson.getInt("dealStatus");

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();

			conn = DBConnector.getInstance().getDealershipConnection();

			// TODO具体逻辑
			//这里引用的jar要是一个版本的，否则更细心代码下来都编译报错了 modify:songhe
			JSONArray data = dealerShipEditService.loadWorkListService(chainCode, conn, userId, dealStatus);

			return new ModelAndView("jsonView", success(data));

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
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int resultId = dataJson.getInt("resultId");
			dealerShipEditService.clearRelatedPoi(resultId,userId);
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		} 
	}
  
  @RequestMapping(value = "/saveData")
	public ModelAndView saveData(HttpServletRequest request) throws Exception {

		try {
			JSONObject parameter = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (parameter == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();
			//保存数据
			OperationResult opResult=dealerShipEditService.saveDataService(parameter,userId);
			//执行检查
			int resultCount=dealerShipEditService.runDealershipCheck(parameter);
			
			Map<String,Integer> result = new HashMap<>();
			result.put("checkLogs", resultCount);

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
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
			
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
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

			return new ModelAndView("jsonView", success(data));
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
	
	
	@RequestMapping(value = "/passDealership")
	public ModelAndView passDealership(HttpServletRequest request) {
		try {
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			JSONArray resultIds=jsonObj.getJSONArray("resultIds");
			dealerShipEditService.passDealership(userId,resultIds);			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("转内业失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	
	@RequestMapping(value = "/impConfirmData")
	public ModelAndView impConfirmData(HttpServletRequest request) {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token"); 
			dealerShipEditService.impConfirmData(request,tokenObj.getUserId());
			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value = "/queryChainDetail")
	public ModelAndView queryChainDetail(HttpServletRequest request) {
		try {
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			String chainCode=jsonObj.getString("chainCode");
			Map<String,Object> result = dealerShipEditService.queryChainDetail(chainCode);
			return new ModelAndView("jsonView", success(result));
		} catch (Exception e) {
			logger.error("转内业失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	/**
	 * 关闭作业
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/closeWork")
	public ModelAndView closeWork(HttpServletRequest request) {
		try {
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			JSONArray resultIds=jsonObj.getJSONArray("resultIds");
			dealerShipEditService.closeWork(userId,resultIds);			
			return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("关闭作业，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", exception(e));
		}
	}
	
	@RequestMapping(value="/closeChain")
	public ModelAndView closeChain(HttpServletRequest request) throws Exception {
		Connection conn = null;
		try {
			JSONObject jsonObj = JSONObject.fromObject(request.getParameter("parameter"));
			if (jsonObj == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}

			String chainCode = jsonObj.getString("chainCode");
			conn = DBConnector.getInstance().getDealershipConnection();
			dealerShipEditService.closeChainService(conn, chainCode);

		    return new ModelAndView("jsonView", success());
		} catch (Exception e) {
			logger.error("关闭品牌，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}finally{
			if(conn!=null){
				conn.close();
			}
		}
	}
	
	/**
	 * 下拉省市列表
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getAdminCodeAndProvince")
	public ModelAndView getAdminCodeAndProvince(HttpServletRequest request) throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		try {
			JSONArray data = manApi.getAdminCodeAndProvince();//得到distinct过后的adminCode列表

		    return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}finally{
		}
	}
	
	
	/**
	 * 编辑查询接口
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/queryByCon")
	public ModelAndView queryByCon(HttpServletRequest request) throws Exception {
		
		try {
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			JSONArray data = dealerShipEditService.queryByCon(jsonObj);

		    return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}finally{
		}
	}
	

	
	/**
	 * 补充数据接口
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/addChainData")
	public ModelAndView addChainData(HttpServletRequest request)throws ServletException, IOException {
		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();

			Map<String, Object> result = dealerShipEditService.addChainData(request, userId);	
			
			List<Integer> resultIdList = (List<Integer>) result.get("resultIdList");
			List<String> chainCodeList = (List<String>)result.get("chainCodeList");
			
			JobApi jobApi = (JobApi) ApplicationContextUtil.getBean("jobApi");
			JSONObject jobReq = new JSONObject();
			jobReq.put("resultIdList", resultIdList);
			jobReq.put("chainCodeList", chainCodeList);
			jobReq.put("userId", userId);
			
			long jobId = jobApi.createJob("dealershipAddChainDataJob", jobReq, userId,0, "代理补充数据job");
			
			return new ModelAndView("jsonView", success(jobId));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
      }}

	/**
	 * 加载poi属性，用于代理店保存冲突检测
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/loadPoiForConflict")
	public ModelAndView loadPoiForConflict(HttpServletRequest request) throws Exception {
		
		try {
			JSONObject jsonObj=JSONObject.fromObject(request.getParameter("parameter"));
			if(jsonObj==null){
				throw new IllegalArgumentException("parameter参数不能为空。"); 
			}
			JSONObject data = dealerShipEditService.loadPoiForConflict(jsonObj);

		    return new ModelAndView("jsonView", success(data));
		} catch (Exception e) {
			logger.error("查询失败，原因：" + e.getMessage(), e);
			return new ModelAndView("jsonView", fail(e.getMessage()));
		}finally{

		}
	}
}