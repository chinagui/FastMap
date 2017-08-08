package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PhotoImporter {
	public static int run(Connection conn, Statement stmt, JSONObject poi, Map<String, Map<String, Photo>> photoes)
			throws Exception {

		JSONArray array = poi.getJSONArray("attachments");

		IxPoiPhoto ixPhoto = new IxPoiPhoto();

		Photo photo = new Photo();
		
		if(array.size() == 0) return 0;

		int result = 0;
		
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);

			int tag = obj.getInt("tag");
			
			int type = obj.getInt("type");
			
			String url = obj.getString("url");

			if (tag != 7 || type != 1) {
				continue;
			}

			ixPhoto.setPoiPid(poi.getInt("pid"));

			String fccPid = UuidUtils.genUuid();

			ixPhoto.setFccPid(fccPid);

			ixPhoto.setTag(tag);

			BasicOperator operator = new BasicOperator(conn, ixPhoto);

			operator.insertRow2Sql(stmt);

			runPhoto(fccPid, url, photo, photoes);
			
			result++;
		}

		return result;
	}

	/**
	 * 
	 * @param fccPid
	 * @param url
	 * @param photo
	 * @param photoes<文件夹名称，<文件名，照片信息>>
	 * @return
	 */
	public static void runPhoto(String fccPid, String url, Photo photo, Map<String, Map<String, Photo>> photoes) {

		if (url == null || url.isEmpty()) {
			return;
		}

		int index = url.lastIndexOf("/");

		String dir = url.substring(0, index);

		String name = url.substring(index + 1);

		photo.setRowkey(fccPid);

		photo.setA_fileName(name);

		photo.setA_uuid(fccPid);
		
		photo.setA_content(3);

		if (photoes.containsKey(dir)) {

			if (photoes.get(dir).containsKey(name)) {

				photoes.get(dir).values().add(photo);

			} else {
				
				photoes.get(dir).put(name, photo);
			}
		} else {

			Map<String, Photo> item = new HashMap<>();

			item.put(name, photo);

			photoes.put(dir, item);
		}
	}
}
