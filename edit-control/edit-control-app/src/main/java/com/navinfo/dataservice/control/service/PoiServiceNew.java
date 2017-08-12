package com.navinfo.dataservice.control.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.dropbox.iface.DropboxApi;
import com.navinfo.dataservice.api.dropbox.model.UploadInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.engine.editplus.operation.imp.UploadOperationByGather;
import com.navinfo.dataservice.engine.photo.CollectorImport;

public class PoiServiceNew {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private PoiServiceNew() {
	}

	private static class SingletonHolder {
		private static final PoiServiceNew INSTANCE = new PoiServiceNew();
	}

	public static PoiServiceNew getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void logTest(){
		log.info("PoiServiceNew...");
	}
	
	public UploadResult upload(int uploadId,int subtaskId,long userId)throws Exception{
		//1. 解压上传文件
		//1.1 获取上传文件信息
		long t1 = System.currentTimeMillis();
		DropboxApi dropboxApi = (DropboxApi)ApplicationContextUtil.getBean("dropboxApi");
		UploadInfo upInfo = dropboxApi.getUploadInfoByJobId(uploadId);
		if(upInfo==null)throw new Exception("根据uploadId("+uploadId+")未查询到上传文件信息");
		
			//初始化存储图片属性的map
			Map<String, Photo> photoMap=new HashMap<String, Photo>();
		
		//1.2 解压文件
		String filePath = upInfo.getFilePath()+File.separator+upInfo.getUploadId();
		ZipUtils.unzipFile(filePath+ File.separator + upInfo.getFileName(), filePath);
		UploadManager upMan = new UploadManager(userId,filePath + "/poi.txt");
		upMan.setSubtaskId(subtaskId);
		UploadResult result = upMan.upload(photoMap);
		//读取poi文件，导入...
		long t2 = System.currentTimeMillis();
		log.info("poi import total time:"+(t2-t1)+"ms.");
		//2.1 
		//读取照片文件，导入hbase
//		CollectorImport.importPhoto(filePath);
		log.info("photoMap.size(): "+photoMap.size());
		CollectorImport.importPhoto(photoMap,filePath);
		long t3 = System.currentTimeMillis();
		log.info("photo import total time:"+(t3-t2)+"ms.");
		return result;
		
	}
}
