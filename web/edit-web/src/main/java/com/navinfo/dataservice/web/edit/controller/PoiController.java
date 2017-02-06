package com.navinfo.dataservice.web.edit.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.app.download.PoiDownloadOperation;
import com.navinfo.dataservice.control.app.search.Operation;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
//import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
import com.navinfo.dataservice.engine.photo.CollectorImport;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PoiController extends BaseController{
	private static final Logger logger = Logger.getLogger(EditController.class);
	

	/**
	 * @Title: getPoi
	 * @Description: 安卓下载 (修)(第七迭代)(变更 : 增加返回值  poi下载时间)
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月29日 下午8:48:59 
	 */
	@RequestMapping(value = "/poi/base/download")
	public ModelAndView getPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray gridDateList = new JSONArray();
			gridDateList = jsonReq.getJSONArray("grid");
			
			Map<String,String> gridDateMap = new HashMap<String,String>();
			
			for (int i=0;i<gridDateList.size();i++) {
				JSONObject gridDate = gridDateList.getJSONObject(i);
				gridDateMap.put(gridDate.getString("grid"), gridDate.getString("date"));
			}
			
			PoiDownloadOperation operation = new PoiDownloadOperation();
			String url = operation.generateZip(gridDateMap);
			//*********zl 2016.11.29 ***********
			String poisDownloadDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());//增加抽取时间 poisDownloadDate
			Map<String,String> dateMap = new HashMap<String,String>();
			dateMap.put("url", url);
			dateMap.put("downloadDate", poisDownloadDate);
			return new ModelAndView("jsonView", success(dateMap));
			//*********zl 2016.11.29 ***********
			//return new ModelAndView("jsonView", success(url));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 安卓上传
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/upload")
	public ModelAndView importPoi(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		
		try{
			Date startTime = new Date();
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");
			
			Long userId = tokenObj.getUserId();
			
			String filePath = unzipByJobId(jobId,userId);
			UploadOperationByGather operation = new UploadOperationByGather(userId);
			//UploadOperation operation = new UploadOperation(userId);
			JSONObject retArray = operation.importPoi(filePath + "/poi.txt");
			Date endTime = new Date();
			logger.info("poi import total time:"+ (endTime.getTime() - startTime.getTime())+"ms");
			startTime = new Date();
			CollectorImport.importPhoto(filePath);
			endTime = new Date();
			logger.info("photo import total time:"+ (endTime.getTime() - startTime.getTime())+"ms");
			return new ModelAndView("jsonView", success(retArray));
		}catch(Exception e){
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
		
	}
	
	/**
	 * 安卓端检查是否有可下载的POI
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/poi/base/download/check")
	public ModelAndView downloadCheck(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			JSONArray gridDateList = new JSONArray();
			gridDateList = jsonReq.getJSONArray("grid");
			
			Operation operation = new Operation();
			JSONArray ret = operation.downloadCheck(gridDateList);
			
			return new ModelAndView("jsonView", success(ret));
		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
	
	
	/**
	 * 根据rowId获取POI（返回名称和分类）
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	// 此接口采集端不需要，已跟刘妍希确认 20161025
//	@RequestMapping(value = "/poi/base/getByRowId")
//	public ModelAndView getByRowId(HttpServletRequest request,
//			HttpServletResponse response) throws ServletException, IOException {
//		String parameter = request.getParameter("parameter");
//		try{
//			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
//			JSONObject jsonReq = JSONObject.fromObject(parameter);
//
//			String rowId = jsonReq.getString("rowId");
//			double x = jsonReq.getDouble("x");
//			double y = jsonReq.getDouble("y");
//			
//			Operation operation = new Operation();
//			
//			JSONObject ret = operation.getByRowId(rowId, x, y);
//			
//			return new ModelAndView("jsonView", success(ret));
//		} catch (Exception e) {
//			String logid = Log4jUtils.genLogid();
//
//			Log4jUtils.error(logger, logid, parameter, e);
//
//			return new ModelAndView("jsonView", fail(e.getMessage()));
//		}
//	}
//	
	
	
	private String unzipByJobId(int jobId,Long userId) throws Exception{
		
		UploadOperation operation = new UploadOperation(userId);
		
		JSONObject uploadInfo = operation.getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;

		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}
	

}
