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
import com.navinfo.dataservice.engine.dropbox.manger.UploadManager;
import com.navinfo.dataservice.engine.fcc.trackline.TrackLinesUpload;

@Controller
public class TrackLineController extends BaseController {

	private static final Logger logger = Logger.getLogger(TrackLineController.class);

	@RequestMapping(value = "/trackline/import")
	public ModelAndView imporTrackLine(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadManager upload = new UploadManager();

			String filePath = upload.unzipByJobId(jobId);

			TrackLinesUpload tipsUploader = new TrackLinesUpload();

			tipsUploader.run(filePath + "/"+ "tips.txt");

			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getResultJsonArr());

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}
}
