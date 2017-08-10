package com.navinfo.dataservice.engine.dropbox.manger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.photo.RotateImageUtils;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.photo.HBaseController;
import com.navinfo.dataservice.engine.dropbox.dao.DBController;
import com.navinfo.dataservice.engine.dropbox.util.DropboxUtil;

public class UploadService {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	private static class SingletonHolder {
		private static final UploadService INSTANCE = new UploadService();;
	}

	public static UploadService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
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
	 * @return 
	 * @throws FileUploadException 
	 * @throws UnsupportedEncodingException 
	 */
	public HashMap<Object,Object>  uploadResource(HttpServletRequest request) throws Exception {
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<FileItem> items = upload.parseRequest(request);
		Iterator<FileItem> it = items.iterator();
		
		int pid = 0;
		int dbId = 0;
		String fileType = "";
		String userName = "";
		String userId = "";
		
		FileItem uploadItem = null;
			
		while(it.hasNext()){
			FileItem item = it.next();
			
			if (item.isFormField()){
				if ("parameter".equals(item.getFieldName())) {
					String param = item.getString("UTF-8");
					JSONObject jsonParam = JSONObject.fromObject(param);
					if(jsonParam.containsKey("pid")){
						pid = jsonParam.getInt("pid");
					}
					if(jsonParam.containsKey("dbId")){
						dbId = jsonParam.getInt("dbId");
					}
					if(jsonParam.containsKey("filetype")){
						fileType = jsonParam.getString("filetype");
					}
					if(jsonParam.containsKey("userName")){
						userName = jsonParam.getString("userName");
					}
					if(jsonParam.containsKey("userId")){
						userId = jsonParam.getString("userId");
					}
				}
				
			}else{
				if (item.getName()!= null && !item.getName().equals("")){
					uploadItem = item;
				}else{
					throw new Exception("上传的文件格式有问题！");
				}
			}
		}
		AccessToken tokenObj = (AccessToken) request.getAttribute("token");
		userId = String.valueOf(tokenObj.getUserId());
		
		if(fileType.equals("photo")){
			InputStream fileStream = uploadItem.getInputStream();
			DBController dbController = new DBController();
			HBaseController hbaseController = new HBaseController();
				
			//调用hadoop方法传输文件流，userId,经纬度，获取photo_id
			String photoId = hbaseController.putPhoto(fileStream,dbId,userId,pid);
			
			HashMap<Object,Object> data = new HashMap<Object,Object>();
			
			//由前端在保存POI时维护
			//dbController.insertIxPoiPhoto(dbId,pid,photoId);
			
			data.put("PID", photoId);
			return data;
		}else if(fileType.equals("android_log")){//安卓端日志
			HashMap<Object,Object> data = new HashMap<Object,Object>();
			//"dropbox.upload.path"
			String logUploadDir = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath)+"/log";  //服务器部署路径 /data/resources/upload
			logUploadDir+="/"+userName+"_"+userId;
			File tempFile = new File(uploadItem.getName());
			File file = new File(logUploadDir,tempFile.getName());
			File fileParent = file.getParentFile();
			if(!fileParent.exists()){
				fileParent.mkdirs();
			}
			if(!file.exists()){
			    file.createNewFile(); 
			}
			uploadItem.write(file);
			
			data.put("url", logUploadDir+"/"+tempFile.getName());
			return data;
		}
		
		return null;

	}
	
	
	
	public String uploadFile(String urlString, String fileName, InputStream fileStream) throws IOException{
		File file = new File(urlString);
		
		if (!file.exists()) {
			file.mkdirs();
		}
		
	    //读取文件上传到服务器
	    byte[]bytes=new byte[1024];
	    
	  //选择你要存放的文件
        FileOutputStream out=new FileOutputStream("f:\\poi00333.txt");
	    int numReadByte=0;
	    while((numReadByte=fileStream.read(bytes,0,1024))>0)
	    {
	        out.write(bytes, 0, numReadByte);
	    }
	
	    out.flush();
	    fileStream.close();
	    
	    String result = null ;
	    return result;
	}
	
	public String uploadInfoFile(String urlString, String fileName, String filePath,String subtaskId,long userId) throws IOException{
		URL url=new URL(urlString);
	    HttpURLConnection connection=(HttpURLConnection)url.openConnection();
	    connection.setDoInput(true);
	    connection.setDoOutput(true);
	    connection.setRequestMethod("POST");
	    connection.addRequestProperty("FileName", fileName);
	    connection.addRequestProperty("subtaskId",subtaskId);
	    connection.addRequestProperty("userId",String.valueOf(userId));
	    connection.setRequestProperty("content-type", "text/plain;charset=UTF-8");
	    connection.setConnectTimeout(Integer.valueOf(SystemConfigFactory.getSystemConfig().getValue(PropConstant.inforTimeOut)));
	    BufferedOutputStream  out=new BufferedOutputStream(connection.getOutputStream());
	    
	    //读取文件上传到服务器
	    File file=new File(filePath+"/"+fileName);
	    FileInputStream fileInputStream=new FileInputStream(file);
	    byte[]bytes=new byte[1024];
	    
	    int numReadByte=0;
	    while((numReadByte=fileInputStream.read(bytes,0,1024))>0)
	    {
	        out.write(bytes, 0, numReadByte);
	    }
	
	    out.flush();
	    fileInputStream.close();
	    //读取URLConnection的响应
	    String result = "";
	    BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
	    
	    return result;
	}
	public static void main(String[] args) throws IOException {
		
		/*FileItem uploadItem = new 
		
		File file = new File("f:/","hhhh.txt");
		
		uploadItem.write(file);
		
		String zipFileName =userName+"_"+userId+"_log_"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
		System.out.println("zipFileName: "+zipFileName);
		ZipUtils.zipFile(logUploadDir,logUploadDir+"/"+zipFileName);
        System.exit(0);*/
	}
	/*public static void main(String[] args) throws IOException {
//		String url = SystemConfigFactory.getSystemConfig().getValue(PropConstant.inforUploadUrl);
		String url = "f:";
		String fileName = "poi003.txt";
		String filePath = "f:/poi004.txt";
		System.out.println(UploadService.getInstance().uploadFile(url, fileName, filePath));
		
		System.exit(0);
	}*/
}
