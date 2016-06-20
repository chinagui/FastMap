package com.navinfo.dataservice.engine.dropbox.manger;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.engine.dropbox.dao.DBController;
import com.navinfo.dataservice.engine.dropbox.util.DropboxUtil;
import com.navinfo.dataservice.dao.photo.HBaseController;

public class UploadManager {

	public List<Integer> checkChunk(int jobId) throws Exception {

		DBController controller = new DBController();

		List<Integer> chunkList = controller.getChunkList(jobId);

		return chunkList;
	}

	public boolean finishUpload(int jobId) throws Exception {

		DBController controller = new DBController();

		JSONObject jsonRow = controller.getUploadInfo(jobId);

		String filePath = jsonRow.getString("filePath") + "/" + jobId;

		String fileName = jsonRow.getString("fileName");

		String md5 = jsonRow.getString("md5");

		DropboxUtil.integrateFiles(filePath, fileName, md5, jobId);

		controller.updateUploadEndDate(jobId);

		return true;
	}

	public int startUpload(String fileName, String md5, int fileSize,
			int chunkSize) throws Exception {

		DBController controller = new DBController();

		int jobId = controller.addUploadRecord(fileName, md5, fileSize,
				chunkSize);

		String uploadPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.uploadPath);

		File file = new File(uploadPath + "/" + jobId);

		file.mkdir();

		return jobId;
	}
	
	public void uploadChunk(HttpServletRequest request) throws Exception{

		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<FileItem> items = upload.parseRequest(request);
		
		Iterator<FileItem> it = items.iterator();
		
		int chunkNo = 0;
		
		int jobId = 0;
		
		FileItem uploadItem = null;
		
		while(it.hasNext()){
			FileItem item = it.next();
			
			if (item.isFormField()){
				
				if ("parameter".equals(item.getFieldName())) {
					String param = item.getString("UTF-8");
					JSONObject jsonParam = JSONObject.fromObject(param);
					jobId = jsonParam.getInt("jobId");
					chunkNo = jsonParam.getInt("chunkNo");
				}
				
			}else{
				if (item.getName()!= null && !item.getName().equals("")){
					uploadItem = item;
				}else{
					throw new Exception("上传的文件格式有问题！");
				}
			}
		}
		
		File tempFile = new File(uploadItem.getName());
		
		String uploadPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.uploadPath);
		
		File file = new File(uploadPath+"/"+jobId,chunkNo+"_"+tempFile.getName());
		
		uploadItem.write(file);
		
		DBController controller = new DBController();
		
		controller.updateProgress(jobId);
		
		controller.insertChunk(jobId, chunkNo);
		
	}
	
	public String unzipByJobId(int jobId) throws Exception{
		
		DBController controller = new DBController();
		
		JSONObject uploadInfo = controller.getUploadInfo(jobId);

		String fileName = uploadInfo.getString("fileName");

		String filePath = uploadInfo.getString("filePath") + "/" + jobId;

		ZipUtils.unzipFile(filePath + "/" + fileName, filePath);
		
		return filePath;
	}

	/**
	 * @param request
	 * @throws FileUploadException 
	 * @throws UnsupportedEncodingException 
	 */
	public void uploadResource(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<FileItem> items = upload.parseRequest(request);
		
		Iterator<FileItem> it = items.iterator();
		
		int pid = 0;
		int dbId = 0;
		
		String fileType = "";
		
		String fileName = "";
		
//		int pid = 1;
//		int dbId = 43;
//		
//		String fileType = "photo";
//		
//		String fileName = "photo_test";
		
		FileItem uploadItem = null;
			
		while(it.hasNext()){
			FileItem item = it.next();
			
			if (item.isFormField()){
				
				if ("parameter".equals(item.getFieldName())) {
					String param = item.getString("UTF-8");
					JSONObject jsonParam = JSONObject.fromObject(param);
					pid = jsonParam.getInt("pid");
					fileType = jsonParam.getString("fileType");
					fileName = jsonParam.getString("fileName");
				}
				
			}else{
				if (item.getName()!= null && !item.getName().equals("")){
					uploadItem = item;
				}else{
					throw new Exception("上传的文件格式有问题！");
				}
			}
		}
		
		if(fileType.equals("photo")){
			InputStream fileStream = uploadItem.getInputStream();
			
			DBController dbController = new DBController();
			HBaseController hbaseController = new HBaseController();
				
			//调用hadoop方法传输文件流，userId,经纬度，获取photo_id
			String photoId = hbaseController.putPhoto(fileStream);
				
			dbController.insertIxPoiPhoto(dbId,pid,photoId);
		}
		


	}
}
