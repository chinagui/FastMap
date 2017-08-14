package com.navinfo.dataservice.web.dropbox.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.Log4jUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;

@Controller
public class InforController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(InforController.class);

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value = "/import/infor/")
	public void uploadInfor(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String parameter = request.getParameter("parameter");

		try {
			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");
			String subtaskId ="0";
			if(json.containsKey("subtaskId") && json.getString("subtaskId") != null && StringUtils.isNotEmpty(json.getString("subtaskId"))){
				subtaskId = json.getString("subtaskId");
			}
			logger.info(subtaskId);

			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
			logger.info("userId:"+userId);

			UploadService upload = UploadService.getInstance();

			String filePath = upload.unzipByJobId(jobId);

			String url = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.inforUploadUrl);

			String result = upload.uploadInfoFile(url, "infor.json", filePath,subtaskId,userId, jobId);

			response.getWriter().println(
					ResponseUtils.assembleRegularResult(JSONObject
							.fromObject(result)));
		} catch (Exception e) {
			String logid = Log4jUtils.genLogid();

			Log4jUtils.error(logger, logid, parameter, e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		}

	}

}
