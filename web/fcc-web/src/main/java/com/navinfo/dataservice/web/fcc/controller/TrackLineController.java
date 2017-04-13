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
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;
import com.navinfo.dataservice.engine.fcc.track.TrackLinesUpload;

@Controller
public class TrackLineController extends BaseController {

	private static final Logger logger = Logger.getLogger(TrackLineController.class);

	@RequestMapping(value = "/track/import")
	public ModelAndView imporTrackLine(HttpServletRequest request
			) throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		try {

			JSONObject json = JSONObject.fromObject(parameter);

			int jobId = json.getInt("jobId");

			UploadService upload = UploadService.getInstance();

			String filePath = upload.unzipByJobId(jobId);

			TrackLinesUpload trackUploader = new TrackLinesUpload();

			trackUploader.run(filePath + "/"+ "Datum_Track.json");
			
			/*AdasTackPointUpload trackPointUploader = new AdasTackPointUpload();

			trackPointUploader.run(filePath + "/"+ "AdasTrackPoints.json");*/
			
			JSONObject result = new JSONObject();

			result.put("total", trackUploader.getTotal());

			result.put("failed", trackUploader.getFailed());

			result.put("reasons", trackUploader.getResultJsonArr());

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}
}
