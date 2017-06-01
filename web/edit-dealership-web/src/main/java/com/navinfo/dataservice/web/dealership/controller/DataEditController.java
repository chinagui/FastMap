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
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.dealership.service.DataEditService;

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
	private DataPrepareService dealerShipEditService = DataPrepareService.getInstance();

	@RequestMapping(value = "/dealership/applyData")
	public ModelAndView applyData(HttpServletRequest request) throws Exception {
		Connection conn = null;

		try {
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));
			if (dataJson == null) {
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String chainCode = dataJson.getString("chainCode");

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();

			conn = DBConnector.getInstance().getConnectionById(399);

			int data = dealerShipEditService.applyDataService(chainCode,conn,userId);
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
}
