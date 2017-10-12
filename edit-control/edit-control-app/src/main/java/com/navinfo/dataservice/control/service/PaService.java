package com.navinfo.dataservice.control.service;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.dropbox.iface.DropboxApi;
import com.navinfo.dataservice.api.dropbox.model.UploadInfo;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;

public class PaService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private PaService() {
	}

	private static class SingletonHolder {
		private static final PaService INSTANCE = new PaService();
	}

	public static PaService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void logTest(){
		log.info("PaServiceNew...");
	}
	
	public UploadResult upload(int uploadId,int subtaskId,long userId)throws Exception{
		//1. 解压上传文件
		log.info("start unziping file...");
		long t1 = System.currentTimeMillis();
		String dir = unzip(uploadId);
		if(StringUtils.isEmpty(dir)) throw new Exception("上传目录为空");
		long t2 = System.currentTimeMillis();
		log.info("unzip file finished,total time:"+(t2-t1)+"ms.");
		//2. 读取pa.txt文件
		
		PaUploadManager upMan = new PaUploadManager(userId,dir);
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
