package com.navinfo.dataservice.engine.fcc.tips;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.navicommons.geo.computation.GridUtils;

public class TipsExporter {

	private SolrController solr = new SolrController();

	private String folderName;
s	public TipsExporter() {
	}

	private List<Get> generateGets(String gridId, String date)
			throws Exception {
		List<Get> gets = new ArrayList<Get>();

		Set<String> set = new HashSet<String>();

		String wkt = GridUtils.grid2Wkt(gridId);
		
		List<String> rowkeys = solr.queryTipsMobile(wkt, date,TipsUtils.notExpSourceType);

		for (String rowkey : rowkeys) {
			if (set.contains(rowkey)) {
				continue;
			}

			set.add(rowkey);

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "geometry".getBytes());

			get.addColumn("data".getBytes(), "deep".getBytes());

			get.addColumn("data".getBytes(), "source".getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());

			get.addColumn("data".getBytes(), "feedback".getBytes());

			gets.add(get);
		}


		return gets;
	}

	private JSONArray exportByGets(List<Get> gets, Set<String> patternImages)
			throws Exception {

		JSONArray ja = new JSONArray();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn
				.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Result[] results = htab.get(gets);

		Set<String> photoIdSet = new HashSet<String>();

		List<Get> photoGets = new ArrayList<Get>();

		List<JSONObject> list = new ArrayList<JSONObject>();

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			JSONObject json = new JSONObject();

			String rowkey = new String(result.getRow());

			json.put("rowkey", rowkey);

			String source = new String(result.getValue("data".getBytes(),
					"source".getBytes()));

			json.putAll(JSONObject.fromObject(source));

			String sourceType = json.getString("s_sourceType");

			String deep = new String(result.getValue("data".getBytes(),
					"deep".getBytes()));

			JSONObject deepjson = JSONObject.fromObject(deep);

			if (deepjson.containsKey("agl")) {
				json.put("angle", deepjson.getDouble("agl"));

			} else {
				json.put("angle", 0);
			}

			json.put("deep", deepjson);

			if (deepjson.containsKey("in")) {
				JSONObject in = deepjson.getJSONObject("in");

				json.put("relatedLinkId", in.getString("id"));
			} else if (deepjson.containsKey("f")) {

				JSONObject f = deepjson.getJSONObject("f");
				json.put("relatedLinkId", f.getString("id"));
			} else if (deepjson.containsKey("out")) {

				JSONObject out = deepjson.getJSONObject("out");
				json.put("relatedLinkId", out.getString("id"));
			} else {
				json.put("relatedLinkId", JSONNull.getInstance());
			}

			if (sourceType.equals("1406") || sourceType.equals("1401")) {
				// 需要导出关联的模式图

				if (deepjson.containsKey("ptn")) {
					String ptn = deepjson.getString("ptn");

					if (ptn != null && ptn.length() > 0) {
						patternImages.add(ptn);
					}
				}
			}

			String geometry = new String(result.getValue("data".getBytes(),
					"geometry".getBytes()));

			json.putAll(JSONObject.fromObject(geometry));

			String track = new String(result.getValue("data".getBytes(),
					"track".getBytes()));

			JSONObject trackjson = JSONObject.fromObject(track);

			json.put("t_lifecycle", trackjson.getInt("t_lifecycle"));

			json.put("t_command", trackjson.getInt("t_command"));

			JSONArray tTrackInfo = trackjson.getJSONArray("t_trackInfo");

			JSONObject lastTrackInfo = tTrackInfo.getJSONObject(tTrackInfo
					.size() - 1);

			String lastDate = lastTrackInfo.getString("date");

			int handler = lastTrackInfo.getInt("handler");

			for (int i = tTrackInfo.size() - 1; i >= 0; i--) {
				JSONObject trackinfo = tTrackInfo.getJSONObject(i);

				if (trackinfo.getInt("stage") != 3) {
					lastDate = trackinfo.getString("date");

					handler = trackinfo.getInt("handler");

					break;
				}
			}

			json.put("t_operateDate", lastDate);

			json.put("t_handler", handler);

			json.put("t_status", 0);

			boolean flag = false;

			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));

				JSONArray farray = feedback.getJSONArray("f_array");

				json.put("attachments", farray);

				for (int i = 0; i < farray.size(); i++) {
					JSONObject jo = farray.getJSONObject(i);
					int type = jo.getInt("type");
					if (type != 1) {
						continue;
					}

					flag = true;

					String id = jo.getString("content");
					if (photoIdSet.contains(id)) {
						continue;
					}

					photoIdSet.add(id);

					Get get = new Get(id.getBytes());

					get.addColumn("data".getBytes(), "attribute".getBytes());

					get.addColumn("data".getBytes(), "origin".getBytes());

					photoGets.add(get);

				}
			} else {
				json.put("attachments", new JSONArray());
			}

			if (flag) {
				list.add(json);
			} else {
				ja.add(json);
			}

		}

		if (list.size() > 0) {
			Map<String, JSONObject> photoMap = exportPhotos(photoGets);

			for (int i = 0; i < list.size(); i++) {
				JSONObject json = list.get(i);

				JSONArray feedbacks = json.getJSONArray("attachments");

				JSONArray newFeedbacks = new JSONArray();

				for (int j = 0; j < feedbacks.size(); j++) {

					JSONObject newFeedback = new JSONObject();

					JSONObject feedback = feedbacks.getJSONObject(j);

					int type = feedback.getInt("type");

					String content = feedback.getString("content");

					newFeedback.put("type", type);

					newFeedback.put("content", content);

					if (type != 1) {

						newFeedback.put("id", JSONNull.getInstance());

						newFeedback.put("extContent", JSONNull.getInstance());
					} else {

						newFeedback.put("id", content);

						JSONObject extContent = photoMap.get(content);

						newFeedback.put("extContent", extContent);
					}

					newFeedbacks.add(newFeedback);
				}

				json.put("attachments", newFeedbacks);

				ja.add(json);
			}
		}

		return ja;
	}

	private Map<String, JSONObject> exportPhotos(List<Get> gets)
			throws Exception {

		Map<String, JSONObject> photoMap = new HashMap<String, JSONObject>();

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName
				.valueOf(HBaseConstant.photoTab));

		Result[] results = htab.get(gets);

		for (Result result : results) {
			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			byte[] data = result.getValue("data".getBytes(),
					"origin".getBytes());

			if (data == null || data.length == 0) {
				continue;
			}

			JSONObject attribute = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "attribute".getBytes())));

			JSONObject extContent = new JSONObject();

			extContent.put("latitude", attribute.getDouble("a_latitude"));

			extContent.put("longitude", attribute.getDouble("a_longitude"));

			extContent.put("direction", attribute.getInt("a_direction"));

			extContent.put("shootDate", attribute.getString("a_shootDate"));

			extContent.put("deviceNum", attribute.getString("a_deviceNum"));

			String fileName = this.folderName
					+ attribute.getString("a_fileName");

			photoMap.put(rowkey, extContent);

			FileUtils.makeSmallImage(data, fileName);

		}

		return photoMap;

	}

	/**
	 * 根据网格和时间导出tips
	 * 
	 * @param condition
	 *            网格 、时间戳对象数组 整型
	 * @param date
	 *            时间
	 * @param fileName
	 *            导出文件名
	 * @return 导出个数
	 * @throws Exception
	 */
	public int export(JSONArray condition, String folderName,
			String fileName, Set<String> patternImages) throws Exception {

		int count = 0;

		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		this.folderName = folderName;

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		
		List<Get> getsAll = new ArrayList<>();
		
		for (Object obj : condition) {
			
			JSONObject conJson=JSONObject.fromObject(obj);
			
			String grid=conJson.getString("grid");
			
			String date=conJson.getString("date");
			
			if("null".equalsIgnoreCase(date)){
			    date=null;
			}
			
			List<Get> gets = generateGets(grid, date);
			
			getsAll.addAll(gets);
		}

		JSONArray ja = exportByGets(getsAll, patternImages);

		for (int j = 0; j < ja.size(); j++) {
			pw.println(ja.getJSONObject(j).toString());

			count += 1;
		}

		pw.close();

		return count;
	}

	public static void main(String[] args) throws Exception {
		JSONArray condition = new JSONArray();

		String s ="60560301,60560302,60560303,60560311,60560312,60560313,60560322,60560323,60560331,60560332,60560333,60560320,60560330,60560300,60560321,60560310";
		
		String[] st = s.split(",");
		
		for(String ss : st){
			JSONObject obj=new JSONObject();
			obj.put("grid", ss);
			obj.put("date", "20160912205811");
			condition.add(obj);
		}

		Set<String> images = new HashSet<String>();

		TipsExporter exporter = new TipsExporter();
		System.out.println(exporter.export(condition,
				"F:/07 DataService/test", "1.txt", images));
		System.out.println(images);
		System.out.println("done");
	}
}
