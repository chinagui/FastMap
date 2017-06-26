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
	
//	public UploadResult upload(int uploadId,int subtaskId,long userId)throws Exception{
//		//1. 解压上传文件
//		//1.1 获取上传文件信息
//		long t1 = System.currentTimeMillis();
//		DropboxApi dropboxApi = (DropboxApi)ApplicationContextUtil.getBean("dropboxApi");
//		UploadInfo upInfo = dropboxApi.getUploadInfoByJobId(uploadId);
//		if(upInfo==null)throw new Exception("根据uploadId("+uploadId+")未查询到上传文件信息");
//		//1.2 解压文件
//		String filePath = upInfo.getFilePath()+File.separator+upInfo.getUploadId();
//		ZipUtils.unzipFile(filePath+ "/" + upInfo.getFileName(), filePath);
//		//读取文件，导入...
//		//2.1 
//		
//		return null;
//		
//	}
	
	public JSONObject importPoi(int jobId,int subtaskId,Long userId) throws Exception{
		Date startTime = new Date();
		logger.info("/poi/base/upload jobId : "+ jobId);
		
		String filePath = unzipByJobId(jobId,userId);
		UploadOperationByGather operation = new UploadOperationByGather(userId,subtaskId);
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
//		String filePath = "E:/Users/temp/resources/upload" + "/" + jobId;
		//String filePath = "D:/temp/data/resources/upload" + "/" + jobId;
		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}
	
	
}
