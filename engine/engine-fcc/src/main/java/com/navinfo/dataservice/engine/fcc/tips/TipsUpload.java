package com.navinfo.dataservice.engine.fcc.tips;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.navicommons.geo.computation.GeometryUtils;

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
	
	static int IMPORT_STATE=1;

	private Map<String, JSONObject> insertTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> updateTips = new HashMap<String, JSONObject>();

	private Map<String, JSONObject> oldTips = new HashMap<String, JSONObject>();
	
	private Map<String, JSONObject> roadNameTips = new HashMap<String, JSONObject>(); //道路名tips，用户道路名入元数据库
	
	private static final Logger logger = Logger.getLogger(TipsUpload.class);

	
	private String currentDate;

	private int total;

	private int failed;  //记录导入错误的条数

	private JSONArray reasons;

	private SolrController solr;

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

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Map<String, Photo> photoInfo = new HashMap<String, Photo>();

		List<Get> gets = loadFileContent(fileName, photoInfo);

		solr = new SolrController();

		loadOldTips(htab, gets);

		List<Put> puts = new ArrayList<Put>();

		doInsert(puts);

		doUpdate(puts);

		htab.put(puts);

		htab.close();
		
		//道路名入元数据库
		importRoadNameToMeta();

		return photoInfo;
	}
	
	
	/**
	 * 道路名入元数据库
	 */

	private void importRoadNameToMeta() {
		
		Set<Entry<String, JSONObject>> set = roadNameTips.entrySet();
		Iterator<Entry<String, JSONObject>> it = set.iterator();
		while (it.hasNext()) {
			Entry<String, JSONObject> en = it.next();

			String rowkey = en.getKey();
			// 坐标
			JSONObject nameTipJson = en.getValue();
			JSONObject deep = JSONObject.fromObject(nameTipJson.get("deep"));
			JSONObject geo = deep.getJSONObject("geo");
			JSONArray jaCoords = geo.getJSONArray("coordinates");
			double longitude=jaCoords.getDouble(0);
			double latitude=jaCoords.getDouble(1);
			
			// 名称,调用元数据库接口入库
			MetadataApi metaApi=(MetadataApi) ApplicationContextUtil.getBean("metaApi");
			JSONArray names = deep.getJSONArray("n_array");
			for (Object name : names) {
				if (name != null && StringUtils.isNotEmpty(name.toString())) {
					try {
						metaApi.nameImport(name.toString(), longitude, latitude, rowkey);
					} catch (Exception e) {
						//reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));
						logger.error(e.getMessage(), e.getCause());
					}
				}
			}
			
		}
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
				
				json.put("t_cStatus", 1);
				
				json.put("t_dStatus", 0);
				
				json.put("t_mStatus", 0);
				
				json.put("t_inStatus", 0);
				
				json.put("t_inMeth", 0);
				
				//道路名测线
				if(sourceType.equals("1901")){
					roadNameTips.put(rowkey, json);
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
				logger.error(e.getMessage(),e.getCause());
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
				
				
				//old有则更新
				if (oldTips.containsKey(rowkey)) {

					JSONObject oldTip = oldTips.get(rowkey);
					
					//对比采集时间
					int res = canUpdate(oldTip, json.getString("t_operateDate"));
					if (res < 0) {
						failed += 1;
						//-1表示old已删除
						if (res == -1) {
							reasons.add(newReasonObject(rowkey,
									ErrorType.Deleted));
						} 
						//else =-2表示当前采集时间较旧
						else {
							reasons.add(newReasonObject(rowkey,
									ErrorType.InvalidDate));
						}
						continue;
					}
					
					put = updatePut(rowkey, json, oldTip);

				} else {
					put = insertPut(rowkey, json);
				}

				
				JSONObject solrIndex = TipsUtils.generateSolrIndex(json,currentDate);

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
		
		JSONObject jsonTrack =TipsUtils.generateTrackJson(3, TipsUpload.IMPORT_STATE,json.getInt("t_handler"),
				json.getInt("t_command"), null, json.getString("t_operateDate"),
				json.getInt("t_cStatus"),json.getInt("t_dStatus"),json.getInt("t_mStatus"),
				json.getInt("t_inStatus"),json.getInt("t_inMeth"));

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

				JSONObject solrIndex = TipsUtils.generateSolrIndex(json,currentDate);

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

		JSONObject jsonTrack = TipsUtils.generateTrackJson(lifecycle,TipsUpload.IMPORT_STATE,
				json.getInt("t_handler"), json.getInt("t_command"),
				oldTip.getJSONArray("t_trackInfo"),json.getString("t_operateDate"),
				json.getInt("t_cStatus"),json.getInt("t_dStatus"),json.getInt("t_mStatus"),
				json.getInt("t_inStatus"),json.getInt("t_inMeth"));

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

		JSONArray newFArray = new JSONArray();

		JSONObject graph = null;

		Set<String> picNames = new HashSet<String>();

		for (int i = 0; i < fArray.size(); i++) {
			JSONObject jo = fArray.getJSONObject(i);

			int type = jo.getInt("type");

			if (type == 1) {
				picNames.add(jo.getString("content"));
			}

			if (type == 6) {
				graph = jo;
			} else {
				newFArray.add(jo);
			}
		}

		JSONArray newFeedbacks = json.getJSONArray("feedback");

		for (int i = 0; i < newFeedbacks.size(); i++) {
			JSONObject newFeedback = newFeedbacks.getJSONObject(i);

			int type = newFeedback.getInt("type");
			if (type == 1) {
				if (!picNames.contains(newFeedback.getString("content"))) {
					newFArray.add(newFeedback);
				}
			} else if (type == 6) {
				graph = newFeedback;
			} else {
				newFArray.add(newFeedback);
			}
		}

		if (graph != null) {
			newFArray.add(graph);
		}

		feedback.put("f_array", newFArray);

		json.put("feedback", newFArray);

		put.addColumn("data".getBytes(), "feedback".getBytes(), feedback
				.toString().getBytes());

		return put;
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

		//lifecycle:0（无） 1（删除）2（修改）3（新增） ;
		//stage:0 初始化；1 外业采集；2 内业日编；3 内业月编 ；4 GDB增量
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

		TipsUpload a = new TipsUpload();

		a.run("D:/4.txt");
		System.out.println("成功");
	}
}
