package com.navinfo.dataservice.engine.fcc.tracks;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

/**
 * 保存上传的tips数据
 * 
 * @author lilei3774
 * 
 */
public class TracksUpload {

	private String operateTime;

	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public Map<String, Photo> run(String fileName) throws Exception {

		operateTime = StringUtils.getCurrentTime();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = null;
		Map<String, Photo> photoInfo = null;
		try{
			htab = hbaseConn.getTable(TableName.valueOf("tracks"));
	
			photoInfo = loadFileContent(fileName, htab);
		}catch (Exception e) {
			throw e;
		}finally {
			if(htab!=null){
				htab.close();
			}
		}
		return photoInfo;
	}

	/**
	 * 读取Tips文件，组装Get列表
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private Map<String, Photo> loadFileContent(String fileName, Table htab)
			throws Exception {

		Map<String, Photo> photoInfo = new HashMap<String, Photo>();

		Scanner scanner = new Scanner(new FileInputStream(fileName));

		List<Put> puts = new ArrayList<Put>();

		String segmentId = null;

		int seqNo = 0;
		
		int count = 0;

		while (scanner.hasNextLine()) {
			Put put = null;
			
			JSONObject track = new JSONObject();

			String line = scanner.nextLine();

			JSONObject json = JSONObject.fromObject(line);

			String tmpSegmentId = json.getString("segmentId");

			if (tmpSegmentId.equals(segmentId)) {
				seqNo++;
			} else {
				segmentId = tmpSegmentId;

				seqNo = 1;
			}
			
			track.put("a_segmentId", segmentId);
			
			track.put("a_user", json.getInt("userId"));
			
			track.put("a_seqNum", seqNo);
			
			track.put("a_direction", json.getDouble("direction"));
			
			track.put("a_speed", json.getDouble("speed"));
			
			track.put("a_recordTime", json.getString("recordTime"));
			
			double lng = json.getDouble("longitude");
			
			double lat = json.getDouble("latitude");
			
			track.put("a_longitude", lng);
			
			track.put("a_latitude", lat);
			
			String uuid = UuidUtils.genUuid();
			
			String rowkey = GeoHash.geoHashStringWithCharacterPrecision(lat, lng, 12)
					+ segmentId + uuid;
			
			put = new Put(rowkey.getBytes());

			JSONArray photos = json.getJSONArray("photos");

			JSONArray newPhotos = new JSONArray();

			for (int i = 0; i < photos.size(); i++) {
				JSONObject photoJson = photos.getJSONObject(i);

				Photo photo = getPhoto(photoJson);

				photoInfo.put(photoJson.getString("content"), photo);

				JSONObject newPhoto = new JSONObject();

				newPhoto.put("pUuid", photo.getRowkey());
				
				newPhoto.put("deviceOrient", photoJson.getInt("deviceOrient"));

				newPhotos.add(newPhoto);
			}
			
			track.put("a_photos", newPhotos);
			
			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			puts.add(put);
			
			count++;
			
			if(count>5000){
				htab.put(puts);
				
				puts.clear();
				
				count=0;
			}
		}

		htab.put(puts);

		return photoInfo;

	}

	private Photo getPhoto(JSONObject extContent) {

		Photo photo = new Photo();

		double lng = extContent.getDouble("longitude");

		double lat = extContent.getDouble("latitude");

		String uuid = UuidUtils.genUuid();

		String key = GeoHash.geoHashStringWithCharacterPrecision(lat, lng, 12)
				+ "00" + uuid;

		photo.setRowkey(key);

		photo.setA_uuid(uuid);

		// photo.setA_uploadUser(tip.getString("t_handler"));

		photo.setA_uploadDate(operateTime);

		photo.setA_longitude(lng);

		photo.setA_latitude(lat);

		photo.setA_sourceId(999);

		// photo.setA_sourceCode();

		photo.setA_direction(extContent.getDouble("direction"));

		photo.setA_shootDate(extContent.getString("shootDate"));

		photo.setA_deviceNum(extContent.getString("deviceNum"));

		photo.setA_content(1);

		return photo;
	}

	public static void main(String[] args) throws Exception {

		TracksUpload a = new TracksUpload();

		a.run("C:\\Users\\wangshishuai3966\\Desktop\\2.txt");
	}
}
