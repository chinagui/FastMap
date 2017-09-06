package com.navinfo.dataservice.control.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.dropbox.iface.DropboxApi;
import com.navinfo.dataservice.api.dropbox.model.UploadInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.app.upload.UploadOperation;
import com.navinfo.dataservice.control.model.UploadIxPoi;
import com.navinfo.dataservice.control.model.UploadIxPoiAttachments;
import com.navinfo.dataservice.control.model.UploadIxPoiChargingPole;
import com.navinfo.dataservice.control.model.UploadIxPoiChargingStation;
import com.navinfo.dataservice.control.model.UploadIxPoiContacts;
import com.navinfo.dataservice.control.model.UploadIxPoiFoodtypes;
import com.navinfo.dataservice.control.model.UploadIxPoiGuide;
import com.navinfo.dataservice.control.model.UploadIxPoiHotel;
import com.navinfo.dataservice.control.model.UploadIxPoiIndoor;
import com.navinfo.dataservice.control.model.UploadIxPoiParkings;
import com.navinfo.dataservice.control.model.UploadIxPoiRelateChildren;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
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
		log.info("start unziping file...");
		long t1 = System.currentTimeMillis();
		String dir = unzip(uploadId);
		if(StringUtils.isEmpty(dir)) throw new Exception("上传目录为空");
		long t2 = System.currentTimeMillis();
		log.info("unzip file finished,total time:"+(t2-t1)+"ms.");
		//2. 读取poi.txt文件
		
		UploadManager upMan = new UploadManager(userId,dir);
		upMan.setSubtaskId(subtaskId);
		UploadResult result = upMan.upload();
		return result;
		
	}
	private String unzip(int uploadId)throws Exception{
		DropboxApi dropboxApi = (DropboxApi)ApplicationContextUtil.getBean("dropboxApi");
		UploadInfo upInfo = dropboxApi.getUploadInfoByJobId(uploadId);
		if(upInfo==null)throw new Exception("根据uploadId("+uploadId+")未查询到上传文件信息");
		
		//1.2 解压文件
		String dirPath = upInfo.getFilePath()+File.separator+upInfo.getUploadId();
		ZipUtils.unzipFile(dirPath+ File.separator + upInfo.getFileName(), dirPath);
		return dirPath;
	}
}
