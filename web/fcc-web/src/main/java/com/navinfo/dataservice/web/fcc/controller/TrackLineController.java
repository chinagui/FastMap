package com.navinfo.dataservice.web.fcc.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.engine.fcc.track.AdasTrackPointUpload;

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

			JSONObject json = JSONObject.parseObject(parameter);

			int jobId = json.getInteger("jobId");
			logger.info("开始上传Track, jobId:" + jobId + " parameter:"+parameter);

			UploadService upload = UploadService.getInstance();

			String filePath = upload.unzipByJobId(jobId);

            int total = 0;
            int failed = 0;

            //普通轨迹点
            File fileLine = new File(filePath + "/"+ "track_collection.json");
            if(fileLine.exists()) {
                TrackLinesUpload trackUploader = new TrackLinesUpload();
                trackUploader.run(filePath + "/"+ "track_collection.json",HBaseConstant.trackLineTab);
                total += trackUploader.getTotal();
                failed += trackUploader.getFailed();
            }

            //ADAS轨迹点
			File filePoint = new File(filePath + "/"+ "adas_track_collect.json");
			if(filePoint.exists()) {
                AdasTrackPointUpload trackPointUploader = new AdasTrackPointUpload();
                trackPointUploader.run(filePath + "/"+ "adas_track_collect.json", HBaseConstant.adasTrackPointsTab);
                total += trackPointUploader.getTotal();
                failed += trackPointUploader.getFailed();
            }

			JSONObject result = new JSONObject();
			result.put("total", total);
			result.put("failed", failed);
			result.put("reason", this.getResultObject(failed));

			return new ModelAndView("jsonView", success(result));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		}

	}

	/**
	 * @Description:入库信息（用于接口返回）
	 * @param failed
	 * @return
	 * @author: y
	 * @time:2016-6-30 下午4:50:41
	 */
	private JSONObject getResultObject(int failed) {
		JSONObject json = new JSONObject();
        String status = "成功";
        int remark = 0;
        if(failed > 0) {
            status = "失败";
            remark = 1;
        }
		json.put("status", status);
		json.put("remark", remark);
		return json;
	}
}
