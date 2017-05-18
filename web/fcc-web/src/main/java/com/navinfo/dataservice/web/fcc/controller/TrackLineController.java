package com.navinfo.dataservice.web.fcc.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.engine.fcc.track.AdasTrackPointUpload;
import net.sf.json.JSONArray;
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

            int total = 0;
            int failed = 0;
            JSONArray resultJsonArr = new JSONArray();

            //普通轨迹点
            File fileLine = new File(filePath + "/"+ "track_collection.json");
            if(fileLine.exists()) {
                TrackLinesUpload trackUploader = new TrackLinesUpload();
                trackUploader.run(filePath + "/"+ "track_collection.json",HBaseConstant.trackLineTab);
                total += trackUploader.getTotal();
                failed += trackUploader.getFailed();
                resultJsonArr.addAll(trackUploader.getResultJsonArr());
            }

            //ADAS轨迹点
			File filePoint = new File(filePath + "/"+ "adas_track_collect.json");
			if(filePoint.exists()) {
                AdasTrackPointUpload trackPointUploader = new AdasTrackPointUpload();
                trackPointUploader.run(filePath + "/"+ "adas_track_collect.json", HBaseConstant.adasTrackPointsTab);
                total += trackPointUploader.getTotal();
                failed += trackPointUploader.getFailed();
                resultJsonArr.addAll(trackPointUploader.getResultJsonArr());
            }

			JSONObject result = new JSONObject();
			result.put("total", total);
			result.put("failed", failed);
			result.put("reasons", resultJsonArr);

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}
}
