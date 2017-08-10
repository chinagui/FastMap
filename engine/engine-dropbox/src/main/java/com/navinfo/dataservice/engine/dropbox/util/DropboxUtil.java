package com.navinfo.dataservice.engine.dropbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.navicommons.database.QueryRunner;

public class DropboxUtil {

	public static boolean validateFileMd5(String md5Req,String filePath) throws Exception
	{
		boolean flag = false;
		
		String md5 = DigestUtils.md5Hex(new FileInputStream(new File(filePath)));
		
		if (md5!= null && md5.equals(md5Req)){
			flag = true;
		}
		
		return flag;
	}
	
	public static void integrateFiles(String path,String fileName,String md5,int jobId) throws Exception{
		
		File tmpFile = new File(path+"/"+fileName);
		
		if (tmpFile.exists()){
			return;
		}
		
		File dir = new File(path);
		
		List<String> fileNames = new ArrayList<String>();
		
		File[] files = dir.listFiles();
		
		for(File f : files){
			fileNames.add(f.getName());
		}
		
		Collections.sort(fileNames, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				
				int chunkNo1 = Integer.parseInt(o1.split("_")[0]);
				
				int chunkNo2 = Integer.parseInt(o2.split("_")[0]);
				
				if (chunkNo1 > chunkNo2){
					return 1;
				}else{
					return -1;
				}
				
			}
		});
		
		//整合文件
		FileOutputStream fos = new FileOutputStream(path+"/"+fileName);
		
		for(String fn : fileNames){
			File fin = new File(path+"/"+fn);
			
			FileInputStream fis = new FileInputStream(fin);
			
			byte[] bytes= new byte[8192];
			
			int len = 0;
			
			while((len = fis.read(bytes))!=-1){
				fos.write(bytes, 0, len);
			}
			
			fis.close();
			
//			fin.delete();
			
			fos.flush();
		}
		
		fos.close();
		
		if (!DropboxUtil.validateFileMd5(md5, path+"/"+fileName)){
			throw new Exception("md5校验失败,jobId="+jobId);
		}
	}
	
	public static JSONObject getLastestInfo(String type,String dir, String subdir) throws Exception{
		String[] splits = dir.split("/");
		
		JSONObject json = new JSONObject();
		
		File file = new File(dir);
		
		File[] files = file.listFiles();
		
		long version = 0;
		
		for(File f:files){
			
			if(!f.isFile()){
				continue;
			}
			
			String name = f.getName();
			
			int index= name.indexOf(".");
			
			if(index==-1){
				continue;
			}
			
			long tmpVersion = Long.parseLong(name.substring(0, index));
			
			if (tmpVersion > version){
				version = tmpVersion;
			}
		}
		
		File lastestFile = new File(dir+"/"+version+".zip");
		
		long filesize = lastestFile.length();
		
		json.put("version", String.valueOf(version));
		
		String url = SystemConfigFactory.getSystemConfig().getValue(PropConstant.serverUrl);
		
		if(subdir != null){
			json.put("url", url+type+"/"+subdir+"/"+version+".zip");
		}
		else{
			json.put("url", url+type+"/"+version+".zip");
		}
		
		json.put("filesize", filesize);
		
		return json;
	}
	
	public static JSONArray getNdsList() throws Exception{
		JSONArray array = new JSONArray();
		
		Map<String,String> map = ProvinceUtil.getInstance().getProvinceMap();
		
		SystemConfig config= SystemConfigFactory.getSystemConfig();
		
		String filePath = config.getValue(PropConstant.downloadFilePathNds);
		
		String urlPath = config.getValue(PropConstant.downloadUrlPathNds);
		
		for (Map.Entry<String, String> entry : map.entrySet()) {  
		
			String id = entry.getKey();
			
			String province = entry.getValue();
			
			File file = new File(filePath+"/"+id);
			
			JSONObject json = new JSONObject();
			
			if (file.exists()){
				json = DropboxUtil.getLastestInfo(urlPath, file.getAbsolutePath(), id);	
			}else{
				json.put("filesize", 0);
				
				json.put("url", "");
				
				json.put("version", "");
			}
			
			json.put("id", id);
			
			json.put("name", province);
			
			array.add(json);
		}
		
		return array;
	}
	
	public static JSONArray getGdbList(String version) throws Exception{
		JSONArray array = new JSONArray();
		
		Map<String,String> map = ProvinceUtil.getInstance().getProvinceMap();
		
		SystemConfig config= SystemConfigFactory.getSystemConfig();
		
		String filePath = config.getValue(PropConstant.downloadFilePathBasedata);
		
		String urlPath = config.getValue(PropConstant.downloadUrlPathBasedata);
		
		for (Map.Entry<String, String> entry : map.entrySet()) {  
		
			String id = entry.getKey();
			
			String province = entry.getValue();
			
			File file = new File(filePath+"/"+id);
			
			JSONObject json = new JSONObject();
			
			if (file.exists()){
				json = DropboxUtil.getLastestInfo(urlPath, file.getAbsolutePath(),id);	
			}else{
				json.put("filesize", 0);
				
				json.put("url", "");
				
				json.put("version", "");
				
				json.put("specVersion", version);
			}
			
			json.put("id", id);
			
			json.put("name", province);
			
			json.put("specVersion", version);
			
			array.add(json);
		}
		
		return array;
	}
	
	
	public static JSONObject getAppVersion(int type,String platform) throws Exception{
		JSONObject json = new JSONObject();
		Connection conn = null;
		PreparedStatement stmt=null;
		ResultSet rs =null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getConnectionById(11);
			String sql = "select down_url,app_version,app_size from app_version where  app_platform='"+platform+"' and app_type=" + type +"order by RELEASE_DATE desc";

			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
                json.put("filesize", rs.getInt("app_size"));
				
				json.put("url", rs.getString("down_url"));
				
				json.put("version",  rs.getString("app_version"));
			
				break;
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn, stmt, rs);
		}
		return json;
	}
	
}
