package com.navinfo.dataservice.control.service;

import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
import com.navinfo.dataservice.engine.photo.CollectorImport;

public class PoiService {
	private Logger logger = LoggerRepos.getLogger(this.getClass());

	private PoiService() {
	}

	private static class SingletonHolder {
		private static final PoiService INSTANCE = new PoiService();
	}

	public static PoiService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public JSONObject importPoi(int jobId,Long userId) throws Exception{
		Date startTime = new Date();
		logger.info("/poi/base/upload jobId : "+ jobId);
		
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
		return retArray;
	}
	
	private String unzipByJobId(int jobId,Long userId) throws Exception{
		
		UploadOperation operation = new UploadOperation(userId);
		
		JSONObject uploadInfo = operation.getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;
		//String filePath = "D:/temp/data/resources/upload" + "/" + jobId;
		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}
}
