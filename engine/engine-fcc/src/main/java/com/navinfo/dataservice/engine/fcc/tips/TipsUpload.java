package com.navinfo.dataservice.engine.fcc.tips;

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

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 保存上传的tips数据
 * 
 */

class ErrorType {
	static int InvalidLifecycle = 1;

	static int Deleted = 2;

	static int InvalidDate = 3;

	static int Notexist = 4;

	static int InvalidData = 5;
}

public class TipsUpload {

	private Map<String, JSONObject> insertTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> updateTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> oldTips = new HashMap<String, JSONObject>();
	
	private GeometryFactory factory = new GeometryFactory();

	private String currentDate;

	private int total;

	private int failed;

	private JSONArray reasons;

	private WKTReader reader = new WKTReader();

	private SolrBulkUpdater solr;

	public JSONArray getReasons() {
		return reasons;
	}

	public void setReasons(JSONArray reasons) {
		this.reasons = reasons;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	/**
	 * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public Map<String, Photo> run(String fileName) throws Exception {

		total = 0;

		failed = 0;

		reasons = new JSONArray();

		currentDate = StringUtils.getCurrentTime();

		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

		Map<String, Photo> photoInfo = new HashMap<String, Photo>();

		List<Get> gets = loadFileContent(fileName, photoInfo);

		solr = new SolrBulkUpdater(total * 2, 1);

		loadOldTips(htab, gets);

		List<Put> puts = new ArrayList<Put>();

		doInsert(puts);

		doUpdate(puts);

		solr.commit();

		solr.close();

		htab.put(puts);

		htab.close();

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

			total += 1;

			String rowkey = "";

			try {

				String line = scanner.nextLine();

				JSONObject json = JSONObject.fromObject(line);

				rowkey = json.getString("rowkey");

				int lifecycle = json.getInt("t_lifecycle");

				if (lifecycle == 0) {
					failed += 1;

					reasons.add(newReasonObject(rowkey,
							ErrorType.InvalidLifecycle));

					continue;
				}

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

				String sourceType = json.getString("s_sourceType");

				if (sourceType.equals("2001")) {
					JSONObject glocation = json.getJSONObject("g_location");

					double length = GeometryUtils.getLinkLength(GeoTranslator
							.geojson2Jts(glocation));

					JSONObject deep = JSONObject.fromObject(json
							.getString("deep"));

					deep.put("len", length);

					json.put("deep", deep);
				}

				if (3 == lifecycle) {
					insertTips.put(rowkey, json);
				} else {
					updateTips.put(rowkey, json);
				}

				Get get = new Get(rowkey.getBytes());

				get.addColumn("data".getBytes(), "track".getBytes());

				get.addColumn("data".getBytes(), "feedback".getBytes());

				gets.add(get);

			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}

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

			try {
				JSONObject jo = new JSONObject();

				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));

				jo.putAll(JSONObject.fromObject(track));

				if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", JSONNull.getInstance());
				}

				oldTips.put(rowkey, jo);
			} catch (Exception e) {
				e.printStackTrace();
			}
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

			String rowkey = "";

			try {
				Entry<String, JSONObject> en = it.next();

				rowkey = en.getKey();

				JSONObject json = en.getValue();

				Put put = null;

				if (oldTips.containsKey(rowkey)) {

					JSONObject oldTip = oldTips.get(rowkey);

					int res = canUpdate(oldTip, json.getString("t_operateDate"));
					if (res < 0) {
						failed += 1;

						if (res == -1) {
							reasons.add(newReasonObject(rowkey,
									ErrorType.Deleted));
						} else {
							reasons.add(newReasonObject(rowkey,
									ErrorType.InvalidDate));
						}
						continue;
					}

					put = updatePut(rowkey, json, oldTip);

				} else {
					put = insertPut(rowkey, json);
				}

				JSONObject solrIndex = generateSolrIndex(json);

				solr.addTips(solrIndex);

				puts.add(put);

			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}
		}
	}

	private Put insertPut(String rowkey, JSONObject json) {

		Put put = new Put(rowkey.getBytes());

		JSONObject jsonTrack = generateTrackJson(3, json.getInt("t_handler"),
				json.getInt("t_command"), null, json.getString("t_operateDate"));

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();
		JSONObject jsonSource = new JSONObject();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

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
			String rowkey = "";

			try {
				Entry<String, JSONObject> en = it.next();

				rowkey = en.getKey();

				if (!oldTips.containsKey(rowkey)) {
					failed += 1;

					reasons.add(newReasonObject(rowkey, ErrorType.Notexist));

					continue;
				}

				JSONObject oldTip = oldTips.get(rowkey);

				JSONObject json = en.getValue();

				int res = canUpdate(oldTip, json.getString("t_operateDate"));
				if (res < 0) {
					failed += 1;

					if (res == -1) {
						reasons.add(newReasonObject(rowkey, ErrorType.Deleted));
					} else {
						reasons.add(newReasonObject(rowkey,
								ErrorType.InvalidDate));
					}

					continue;
				}

				Put put = updatePut(rowkey, json, oldTip);

				JSONObject solrIndex = generateSolrIndex(json);

				solr.addTips(solrIndex);

				puts.add(put);
			} catch (Exception e) {
				failed += 1;

				reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

				e.printStackTrace();
			}
		}
	}

	private Put updatePut(String rowkey, JSONObject json, JSONObject oldTip) {

		Put put = new Put(rowkey.getBytes());

		int lifecycle = json.getInt("t_lifecycle");

		JSONObject jsonTrack = generateTrackJson(lifecycle,
				json.getInt("t_handler"), json.getInt("t_command"),
				oldTip.getJSONArray("t_trackInfo"),
				json.getString("t_operateDate"));

		put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
				.toString().getBytes());

		JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();

		Iterator<String> itKey = jsonSourceTemplate.keys();

		JSONObject jsonSource = new JSONObject();

		while (itKey.hasNext()) {

			String key = itKey.next();
			jsonSource.put(key, json.get(key));
		}

		put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
				.toString().getBytes());

		JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

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
			int command, JSONArray oldTrackInfo, String t_operateDate) {

		JSONObject jsonTrack = new JSONObject();

		jsonTrack.put("t_lifecycle", lifecycle);

		jsonTrack.put("t_command", command);

		jsonTrack.put("t_date", currentDate);

		JSONObject jsonTrackInfo = new JSONObject();

		jsonTrackInfo.put("stage", 1);

		jsonTrackInfo.put("date", t_operateDate);

		jsonTrackInfo.put("handler", handler);

		if (null == oldTrackInfo) {

			oldTrackInfo = new JSONArray();
		}

		oldTrackInfo.add(jsonTrackInfo);

		jsonTrack.put("t_trackInfo", oldTrackInfo);

		return jsonTrack;
	}

	private JSONObject generateSolrIndex(JSONObject json) throws Exception {

		JSONObject index = new JSONObject();

		index.put("id", json.getString("rowkey"));

		index.put("stage", 1);

		index.put("t_date", currentDate);

		index.put("t_operateDate", json.getString("t_operateDate"));

		index.put("t_lifecycle", json.getInt("t_lifecycle"));

		index.put("t_command", json.getInt("t_command"));

		index.put("handler", json.getInt("t_handler"));

		index.put("s_sourceType", json.getString("s_sourceType"));

		index.put("s_sourceCode", json.getInt("s_sourceCode"));

		index.put("g_guide", json.getJSONObject("g_guide"));

		JSONObject geojson = json.getJSONObject("g_location");

		index.put("g_location", geojson);

		index.put("deep", json.getString("deep"));

		String sourceType = json.getString("s_sourceType");

		if (sourceType.equals("1501")) {

			JSONObject deep = JSONObject.fromObject(json.getString("deep"));

			JSONObject gSLoc = deep.getJSONObject("gSLoc");

			JSONObject gELoc = deep.getJSONObject("gELoc");

			Geometry g1 = GeoTranslator.geojson2Jts(gSLoc);

			Geometry g2 = GeoTranslator.geojson2Jts(gELoc);

			Geometry g3 = g1.union(g2);

			Geometry g = factory.createMultiPoint(g3.getCoordinates());
		
			index.put("wkt", GeoTranslator.jts2Wkt(g));
		} else if(sourceType.equals("1501")){
			JSONObject deep = JSONObject.fromObject(json.getString("deep"));
			
			JSONArray a = deep.getJSONArray("g_array");
			
			Geometry[] geos = new Geometry[a.size()];
			
			for(int i=0;i<a.size();i++){
				JSONObject geo = a.getJSONObject(i);
				
				geos[i] = GeoTranslator.geojson2Jts(geo);
			}
			
			Geometry g = factory.createGeometryCollection(geos);
			
			index.put("wkt", GeoTranslator.jts2Wkt(g));
		}
		else {

			String wkt = GeoTranslator.jts2Wkt(GeoTranslator
					.geojson2Jts(geojson));

			Geometry g = reader.read(wkt);

			if (!g.isValid()) {
				throw new Exception("invalid g_location");
			}

			index.put("wkt", wkt);
		}
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

	private int canUpdate(JSONObject oldTips, String operateDate) {
		int lifecycle = oldTips.getInt("t_lifecycle");

		JSONArray tracks = oldTips.getJSONArray("t_trackInfo");

		JSONObject lastTrack = tracks.getJSONObject(tracks.size() - 1);

		String lastDate = lastTrack.getString("date");

		int lastStage = lastTrack.getInt("stage");

		if (lifecycle == 1 && lastStage == 4) {

			return -1;
		}

		if (operateDate.compareTo(lastDate) <= 0) {
			return -2;
		}

		return 0;
	}

	private JSONObject newReasonObject(String rowkey, int type) {

		JSONObject json = new JSONObject();

		json.put("rowkey", rowkey);

		json.put("type", type);

		return json;
	}

	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseAddress("192.168.3.156");

		TipsUpload a = new TipsUpload();

		a.run("C:/1.txt");
	}
}
