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
import com.navinfo.dataservice.engine.dropbox.manger.UploadManager;

@Controller
public class UploadController extends BaseController {
	private static final Logger logger = Logger
			.getLogger(UploadController.class);

	@RequestMapping(value = "/upload/start")
	public ModelAndView start(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			String fileName = json.getString("fileName");

			String md5 = json.getString("md5");

			int fileSize = json.getInt("fileSize");

			int chunkSize = json.getInt("chunkSize");

			UploadManager upload = new UploadManager();

			int jobId = upload.startUpload(fileName, md5, fileSize, chunkSize);

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

			UploadManager upload = new UploadManager();

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

			UploadManager upload = new UploadManager();

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

			UploadManager upload = new UploadManager();

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

			UploadManager upload = new UploadManager();

			HashMap<Object,Object> data = upload.uploadResource(request);
			if(!data.isEmpty()){
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
