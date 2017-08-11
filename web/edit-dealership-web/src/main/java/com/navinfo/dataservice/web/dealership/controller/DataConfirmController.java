package com.navinfo.dataservice.web.dealership.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.control.dealership.service.DataConfirmService;
import com.navinfo.dataservice.control.dealership.service.model.InformationExportResult;

import net.sf.json.JSONObject;

@Controller
public class DataConfirmController extends BaseController {
	private static final Logger logger = Logger.getLogger(DataPrepareController.class);
	private DataConfirmService confirmService = DataConfirmService.getInstance();

	@RequestMapping(value = "/downInfo")
	public void downloadInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		response.setContentType("octets/stream");
		Connection conn = null;

		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			JSONObject jsonObj = JSONObject.fromObject(request.getParameter("parameter"));

			String chain = "";
			if (jsonObj.has("chainCode")) {
				chain = jsonObj.getString("chainCode");
			}

			conn = DBConnector.getInstance().getDealershipConnection();
			List<InformationExportResult> informationList = confirmService.getOutConfirmList(conn, chain);
			ExportExcel<InformationExportResult> excel = new ExportExcel<InformationExportResult>();

//			StringBuilder excelName = new StringBuilder();
//			excelName.append("情报下载");
//			excelName.append(DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
			
			StringBuilder sb = new StringBuilder();
			sb.append(userId).append("_").append(DateUtils.dateToString(new Date(), "yyyyMMddHHmmss")).append("_情报下载导出");
			String excelName = sb.toString();
			response.addHeader("Content-Disposition",
					"attachment;filename=" + new String(excelName.toString().getBytes("gb2312"), "ISO8859-1") + ".xls");

			try {
				OutputStream out = response.getOutputStream();
				excel.exportExcel("情报下载", confirmService.headers, informationList, out, "yyyy-MM-dd");
				out.close();
				logger.info("情报导出成功！");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//return new ModelAndView("jsonView", success());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			//return new ModelAndView("jsonView", fail(e.toString()));
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	@RequestMapping(value="/releaseInfo")
	public ModelAndView releaseInfoController(HttpServletRequest request) throws Exception{
		try{
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView", exception("tocken无效"));
			}
			long userId = tokenObj.getUserId();
			
			JSONObject data = confirmService.releaseInfoService(request, userId);
			return new ModelAndView("jsonView",success(data));
		}
		catch(Exception e){
			return new ModelAndView("jsonView",fail(e.toString()));
		}
		//return null;
	}
	
	@RequestMapping(value="/expInfoFeedback")
	public ModelAndView expInfoFeedbackController(HttpServletRequest request) throws Exception{
		try{
			AccessToken tokenObj = (AccessToken)request.getAttribute("token");
			if(tokenObj == null){
				return new ModelAndView("jsonView",exception("tocken无效"));
			}
			
			long userId = tokenObj.getUserId();
			JSONObject obj = JSONObject.fromObject(request.getParameter("parameter"));
			
			String filePath = confirmService.expInfoFeedbackService(userId, obj, request);
			if(filePath.contains("未查到符合条件的情报信息")){
				return new ModelAndView("jsonView",fail(filePath));
			}

			return new ModelAndView("jsonView",success(filePath));
		}
		catch(Exception e){
			return new ModelAndView("jsonView",fail(e.toString()));
		}
	}
}
