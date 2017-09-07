package com.navinfo.dataservice.web.dropbox.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;

@Controller
public class UploadController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(UploadController.class);

	@RequestMapping(value = "/upload/start")
	public ModelAndView start(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {
			AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();

			JSONObject json = JSONObject.fromObject(parameter);

			String fileName = json.getString("fileName");

			String md5 = json.getString("md5");

			int fileSize = json.getInt("fileSize");

			int chunkSize = json.getInt("chunkSize");

			UploadService upload = UploadService.getInstance();

			int jobId = upload.startUpload(fileName, md5, fileSize, chunkSize,userId);

			return new ModelAndView("jsonView", success(jobId));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/upload/check")
	public ModelAndView check(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadService upload = UploadService.getInstance();

			List<Integer> chunkList = upload.checkChunk(jobId);

			return new ModelAndView("jsonView", success(chunkList));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	@RequestMapping(value = "/upload/chunk")
	public ModelAndView chunk(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			UploadService upload = UploadService.getInstance();

			upload.uploadChunk(request);

			return new ModelAndView("jsonView", success());
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}

	@RequestMapping(value = "/upload/finish")
	public ModelAndView finish(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadService upload = UploadService.getInstance();

			upload.finishUpload(jobId);

			return new ModelAndView("jsonView", success());

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}
	
	
	@RequestMapping(value = "/upload/resource")
	public ModelAndView resource(HttpServletRequest request)
			throws ServletException, IOException {

		try {

			UploadService upload = UploadService.getInstance();

			HashMap<Object,Object> data = upload.uploadResource(request);
			logger.info("data : "+data);
			if(data != null && !data.isEmpty()){
				return new ModelAndView("jsonView", success(data));
			}else{
				return new ModelAndView("jsonView", exception("上传失败"));
			}

			
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}
	}
}
