package com.navinfo.dataservice.FosEngine.tips;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.json.JSONException;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.FosEngine.photos.Photo;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.solr.core.SConnection;

/**
 * 保存上传的tips数据
 * 
 * @author lilei3774
 * 
 */
public class TipsUpload {

	private Map<String, JSONObject> insertTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> updateTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> oldTips = new HashMap<String, JSONObject>();
	
	private String currentDate;

	private SConnection solrConn;
	
	public TipsUpload(String solrUrl){
		solrConn = new SConnection(solrUrl);
	}

	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public Map<String, Photo> run(String fileName) throws Exception {
		
		currentDate = StringUtils.getCurrentTime();

		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

		Map<String, Photo> photoInfo = new HashMap<String, Photo>();

		List<Get> gets = loadFileContent(fileName, photoInfo);

		loadOldTips(htab, gets);

		List<Put> puts = new ArrayList<Put>();

		doInsert(puts);

		doUpdate(puts);

		htab.put(puts);

		htab.close();
		
		solrConn.persistentData();

		solrConn.closeConnection();

		return photoInfo;
	}

	/**
	 * 读取Tips文件，组装Get列表
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	private List<Get> loadFileContent(String fileName,
			Map<String, Photo> photoInfo) throws Exception {

		Scanner scanner = new Scanner(new FileInputStream(fileName));

		List<Get> gets = new ArrayList<Get>();

		while (scanner.hasNextLine()) {

			String line = scanner.nextLine();

			JSONObject json = JSONObject.fromObject(line);

			int lifecycle = json.getInt("t_lifecycle");

			if (lifecycle == 0) {
				continue;
			}

			String rowkey = json.getString("rowkey");

			String operateDate = json.getString("t_operateDate");
			
			JSONArray attachments = json.getJSONArray("attachments");

			JSONArray newFeedbacks = new JSONArray();

			for (int i = 0; i < attachments.size(); i++) {
				JSONObject attachment = attachments.getJSONObject(i);

				int type = attachment.getInt("type");

				String content = attachment.getString("content");

				if (1 == type) {

					Photo photo = getPhoto(attachment, json);

					photoInfo.put(content, photo);

					content = photo.getRowkey();
				}

				JSONObject newFeedback = new JSONObject();

				newFeedback.put("user", json.getInt("t_handler"));

				newFeedback.put("userRole", JSONNull.getInstance());

				newFeedback.put("type", type);

				newFeedback.put("content", content);

				newFeedback.put("auditRemark", JSONNull.getInstance());

				newFeedback.put("date", operateDate);

				newFeedbacks.add(newFeedback);
			}

			json.put("feedback", newFeedbacks);

			if (3 == lifecycle) {
				insertTips.put(rowkey, json);
			} else {
				updateTips.put(rowkey, json);
			}

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());

			get.addColumn("data".getBytes(), "feedback".getBytes());

			gets.add(get);

		}

		return gets;
	}

	/**
	 * 从Hbase读取要修改和删除的Tips
	 * 
	 * @param htab
	 * @param gets
	 * @throws Exception
	 */
	private void loadOldTips(Table htab, List<Get> gets) throws Exception {

		if (0 == gets.size()) {
			return;
		}

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			JSONObject jo = new JSONObject();

			String track = new String(result.getValue("data".getBytes(),
					"track".getBytes()));

			jo.putAll(JSONObject.fromObject(track));

			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));

				jo.put("feedback", feedback);
			} else {
				jo.put("feedback", JSONNull.getInstance());
			}

			oldTips.put(rowkey, jo);
		}
	}

	/**
	 * 处理新增的Tips
	 * 
	 * @param puts
	 */
	private void doInsert(List<Put> puts) throws Exception {
		Set<Entry<String, JSONObject>> set = insertTips.entrySet();

		Iterator<Entry<String, JSONObject>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, JSONObject> en = it.next();

			String rowkey = en.getKey();

			JSONObject json = en.getValue();

			if (oldTips.containsKey(rowkey)) {

				JSONObject oldTip = oldTips.get(rowkey);

				Put put = updatePut(rowkey, json, oldTip);

				puts.add(put);

			} else {
				Put put = insertPut(rowkey, json);

				puts.add(put);
			}
			
			JSONObject solrIndex = generateSolrIndex(json);
			
			solrConn.addTips(solrIndex);
		}
	}

	private Put insertPut(String rowkey, JSONObject json) {

		Put put = new Put(rowkey.getBytes());

		JSONObject jsonTrack = generateTrackJson(3, json.getInt("t_handler"),
				null);

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsParse.getSourceConstruct();
		JSONObject jsonSource = new JSONObject();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsParse.getGeometryConstruct();

		itKey = jsonGeomTemplate.keys();

		JSONObject jsonGeom = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonGeom.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
				.toString().getBytes());

		put.addColumn("data".getBytes(), "deep".getBytes(),
				json.getString("deep").getBytes());

		JSONObject feedback = new JSONObject();

		feedback.put("f_array", json.getJSONArray("feedback"));

		put.addColumn("data".getBytes(), "feedback".getBytes(), feedback
				.toString().getBytes());

		return put;
	}

	/**
	 * 处理修改和删除的Tips
	 * 
	 * @param puts
	 */
	private void doUpdate(List<Put> puts) throws Exception {
		Set<Entry<String, JSONObject>> set = updateTips.entrySet();

		Iterator<Entry<String, JSONObject>> it = set.iterator();

		while (it.hasNext()) {
			Entry<String, JSONObject> en = it.next();

			String rowkey = en.getKey();

			if (!oldTips.containsKey(rowkey)) {
				continue;
			}

			JSONObject oldTip = oldTips.get(rowkey);

			JSONObject json = en.getValue();

			Put put = updatePut(rowkey, json, oldTip);

			puts.add(put);
			
			JSONObject solrIndex = generateSolrIndex(json);
			
			solrConn.addTips(solrIndex);

		}
	}

	private Put updatePut(String rowkey, JSONObject json, JSONObject oldTip) {

		Put put = new Put(rowkey.getBytes());

		int lifecycle = json.getInt("t_lifecycle");

		JSONObject jsonTrack = generateTrackJson(lifecycle,
				json.getInt("t_handler"), oldTip.getJSONArray("t_trackInfo"));

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsParse.getSourceConstruct();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		JSONObject jsonSource = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsParse.getGeometryConstruct();

		itKey = jsonGeomTemplate.keys();

		JSONObject jsonGeom = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonGeom.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
				.toString().getBytes());

		put.addColumn("data".getBytes(), "deep".getBytes(),
				json.getString("deep").getBytes());

		JSONObject feedback = oldTip.getJSONObject("feedback");

		if (feedback.isNullObject()) {

			feedback = new JSONObject();

			feedback.put("f_array", new JSONArray());

		}

		JSONArray fArray = feedback.getJSONArray("f_array");

		List<String> picNames = new ArrayList<String>();

		for (int i = 0; i < fArray.size(); i++) {
			JSONObject jo = fArray.getJSONObject(i);

			if (jo.getInt("type") == 1) {
				picNames.add(jo.getString("content"));
			}
		}

		JSONArray newFeedbacks = json.getJSONArray("feedback");

		for (int i = 0; i < newFeedbacks.size(); i++) {
			JSONObject newFeedback = newFeedbacks.getJSONObject(i);

			if (newFeedback.getInt("type") == 1) {
				if (!picNames.contains(newFeedback.getString("content"))) {
					fArray.add(newFeedback);
				}
			} else {
				fArray.add(newFeedback);
			}
		}

		put.addColumn("data".getBytes(), "feedback".getBytes(), feedback
				.toString().getBytes());

		return put;
	}

	/**
	 * 组装Track
	 * 
	 * @param lifecycle
	 * @param handler
	 * @param oldTrackInfo
	 * @return
	 */
	private JSONObject generateTrackJson(int lifecycle, int handler,
			JSONArray oldTrackInfo) {

		JSONObject jsonTrack = new JSONObject();

		jsonTrack.put("t_lifecycle", lifecycle);

		JSONObject jsonTrackInfo = new JSONObject();

		jsonTrackInfo.put("stage", 1);

		jsonTrackInfo.put("date", currentDate);

		jsonTrackInfo.put("handler", handler);

		if (null == oldTrackInfo) {

			oldTrackInfo = new JSONArray();
		}

		oldTrackInfo.add(jsonTrackInfo);

		jsonTrack.put("t_trackInfo", oldTrackInfo);

		return jsonTrack;
	}
	
	private JSONObject generateSolrIndex(JSONObject json) throws Exception{
		
		JSONObject index = new JSONObject();
		
		index.put("id", json.getString("rowkey"));
		
		index.put("stage", 1);
		
		index.put("date", currentDate);
		
		index.put("t_lifecycle", json.getInt("t_lifecycle"));
		
		index.put("t_command", json.getInt("t_command"));
		
		index.put("handler", json.getInt("t_handler"));
		
		index.put("s_sourceType", json.getString("s_sourceType"));
		
		index.put("s_sourceCode", json.getInt("s_sourceCode"));
		
		index.put("g_guide", json.getJSONObject("g_guide"));
		
		JSONObject geojson = json.getJSONObject("g_location");
		
		index.put("g_location", geojson);
		
		index.put("wkt",
				GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(geojson)));
		
		return index;
	}

	private Photo getPhoto(JSONObject attachment, JSONObject tip) {

		Photo photo = new Photo();

		JSONObject extContent = attachment.getJSONObject("extContent");

		double lng = extContent.getDouble("longitude");

		double lat = extContent.getDouble("latitude");

		// String uuid = fileName.replace(".jpg", "");
		//
		// String type = String.format("%02d",
		// Integer.valueOf(tip.getString("s_sourceType")));
		//
		// String key = GeoHash.geoHashStringWithCharacterPrecision(lat, lng,
		// 12)
		// + type + uuid;

		String id = attachment.getString("id");

		photo.setRowkey(id);

		photo.setA_uuid(id.substring(14));

		photo.setA_uploadUser(tip.getInt("t_handler"));

		photo.setA_uploadDate(currentDate);

		photo.setA_longitude(lng);

		photo.setA_latitude(lat);

		// photo.setA_sourceId(tip.getString("s_sourceId"));

		photo.setA_direction(extContent.getDouble("direction"));

		photo.setA_shootDate(extContent.getString("shootDate"));

		photo.setA_deviceNum(extContent.getString("deviceNum"));
		
		photo.setA_fileName(attachment.getString("content"));

		photo.setA_content(1);

		return photo;
	}

	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseAddress("192.168.3.156");

		TipsUpload a = new TipsUpload("http://192.168.4.130:8081/solr/tips/");

		a.run("C:/tips2.txt");
	}
}
