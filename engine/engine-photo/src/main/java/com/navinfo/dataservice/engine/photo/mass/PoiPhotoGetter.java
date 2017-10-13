package com.navinfo.dataservice.engine.photo.mass;

import java.io.IOException;

import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.photo.mass.HBasePoiController;

import net.sf.json.JSONArray;

public class PoiPhotoGetter {

	//获得pid对应的照片rowkey
	public static JSONArray getPoiPhotoRowkey(String param_pid) throws IOException{
			
			String pid = getTenDigitPid(param_pid);
			HBasePoiController hBasePoiController = new HBasePoiController();
			JSONArray rowkeys = hBasePoiController.getRowkeys(pid);
			return rowkeys;
		}
	
	//根据照片rowkey查询照片
	public static byte[] getPhotoByRowkey(String rowkey,String type) {
		HBasePoiController hBasePoiController = new HBasePoiController();
		byte[] photoStream = null;
		try {
			photoStream = hBasePoiController.getPhotoByRowkey(rowkey);
			if ("origin".equals(type)) {
				return FileUtils.rotateOrigin(photoStream);
			} else {
				return FileUtils.makeSmallImage(photoStream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photoStream;
	}
	
		
	//将不满10位的pid，高位补0，返回10位的pid
	private static String getTenDigitPid(String param_pid){
		 int len = param_pid.length();
		 StringBuffer sb = new StringBuffer();
		 while (len < 10) {			 
			 sb.append("0");
			 len++;
		}
		 return sb.append(param_pid).toString();		 
	 }
	
}
