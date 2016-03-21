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
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.GridUtils;
import com.navinfo.dataservice.dao.fcc.SConnection;

public class TipsExporter {

	private SConnection solrConn;
	
	private String folderName;

	public TipsExporter(String solrUrl) {
		solrConn = new SConnection(solrUrl);
	}

	private JSONArray exportByGrid(String gridId, String date) throws Exception {

		JSONArray ja = new JSONArray();

		double mbr[] = GridUtils.grid2Location(gridId);

		String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
				mbr[1], mbr[0], 12);

		String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(mbr[3],
				mbr[2], 12);

		Scanner scanner = HBaseAddress.getHBaseClient().newScanner("tips");

		scanner.setStartKey(startRowkey);

		scanner.setStopKey(stopRowkey);

		scanner.setFamily("data");

		byte[][] qs = new byte[4][];

		qs[0] = "geometry".getBytes();

		qs[1] = "deep".getBytes();

		qs[2] = "source".getBytes();

		qs[3] = "track".getBytes();

		scanner.setQualifiers(qs);

		ArrayList<ArrayList<KeyValue>> rows;

		while ((rows = scanner.nextRows().joinUninterruptibly()) != null) {

			for (List<KeyValue> list : rows) {

				boolean flag = false;

				JSONObject json = new JSONObject();
				for (KeyValue kv : list) {

					String rowkey = new String(kv.key());

					double lonlat[] = GridUtils.geohash2Lonlat(rowkey
							.substring(0, 12));

					if (!flag
							&& !gridId.equals(GridUtils.location2Grid(
									lonlat[0], lonlat[1]))) {
						flag = false;
						break;
					} else {
						flag = true;
					}

					String key = new String(kv.qualifier());

					if ("geometry".equals(key) || "source".equals(key)) {
						JSONObject jo = JSONObject.fromObject(new String(kv
								.value()));

						json.putAll(jo);

					} else if ("deep".equals(key)) {
						JSONObject jo = JSONObject.fromObject(new String(kv
								.value()));

						json.put("deep", jo);

					} else if ("track".equals(key)) {
						JSONObject jo = JSONObject.fromObject(new String(kv
								.value()));
						
						String t_date = jo.getString("t_date");
						
						if (date.compareTo(t_date) > 0) {
							flag = false;
							break;
						}
						
						JSONArray t_trackInfo = jo.getJSONArray("t_trackInfo");

						String t_operateDate = t_trackInfo.getJSONObject(
								t_trackInfo.size() - 1).getString("date");

						json.put("t_lifecycle", jo.getInt("t_lifecycle"));

						json.put("t_operateDate", t_operateDate);

						json.put("rowkey", new String(kv.key()));
					}

				}

				if (flag) {
					json.put("s_project", JSONNull.getInstance());

					json.put("t_status", 0);

					json.put("t_handler", 0);

					json.put("feedback", JSONNull.getInstance());

					ja.add(json);

				}
			}
		}

		return ja;
	}

	private List<Get> generateGets(JSONArray grids, String date)
			throws Exception {
		List<Get> gets = new ArrayList<Get>();

		Set<String> set = new HashSet<String>();

		for (int i = 0; i < grids.size(); i++) {
			String gridId = grids.getString(i);

			String wkt = GridUtils.grid2Wkt(gridId);

			List<String> rowkeys = solrConn.queryTipsMobile(wkt, date);

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

		}

		return gets;
	}

	private JSONArray exportByGets(List<Get> gets) throws Exception{
		
		JSONArray ja = new JSONArray();
		
		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));
		
		Result[] results = htab.get(gets);
		
		Set<String> photoIdSet = new HashSet<String>();
		
		List<Get> photoGets = new ArrayList<Get>();
		
		List<JSONObject> list = new ArrayList<JSONObject>();

		for(Result result : results){
			
			if (result.isEmpty()) {
				continue;
			}
			
			JSONObject json = new JSONObject();
			
			String rowkey = new String(result.getRow());
			
			json.put("rowkey", rowkey);
			
			String deep = new String(result.getValue("data".getBytes(),
					"deep".getBytes()));
			
			JSONObject deepjson = JSONObject.fromObject(deep);
			
			if(deepjson.containsKey("agl")){
				json.put("angle",deepjson.getDouble("agl"));
				
			}
			else{
				json.put("angle", 0);
			}
			
			json.put("deep", deepjson);
			
			String geometry = new String(result.getValue("data".getBytes(),
					"geometry".getBytes()));
			
			json.putAll(JSONObject.fromObject(geometry));
			
			String source = new String(result.getValue("data".getBytes(),
					"source".getBytes()));
			
			json.putAll(JSONObject.fromObject(source));
			
			String track = new String(result.getValue("data".getBytes(),
					"track".getBytes()));
			
			JSONObject trackjson = JSONObject.fromObject(track);
			
			json.put("t_lifecycle", trackjson.getInt("t_lifecycle"));
			
			json.put("t_command", trackjson.getInt("t_command"));

			JSONArray tTrackInfo = trackjson.getJSONArray("t_trackInfo");
			
			JSONObject lastTrackInfo = tTrackInfo.getJSONObject(
					tTrackInfo.size() - 1);
			
			String lastDate = lastTrackInfo.getString("date");
			
			int handler = lastTrackInfo.getInt("handler");
			
			for(int i=tTrackInfo.size()-1; i>=0; i--){
				JSONObject trackinfo = tTrackInfo.getJSONObject(i);
				
				if(trackinfo.getInt("stage") != 3){
					lastDate = trackinfo.getString("date");
					
					handler = trackinfo.getInt("handler");
					
					break;
				}
			}
			
			json.put("t_operateDate", lastDate);
			
			json.put("t_handler", handler);

			json.put("t_status", 0);

			boolean flag=false;
			
			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));
				
				JSONArray farray = feedback.getJSONArray("f_array");

				json.put("attachments", farray);
				
				for(int i=0;i<farray.size();i++){
					JSONObject jo = farray.getJSONObject(i);
					int type = jo.getInt("type");
					if (type != 1){
						continue;
					}
					
					flag = true;
					
					String id = jo.getString("content");
					if(photoIdSet.contains(id)){
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
			
			if (flag){
				list.add(json);
			}
			else{
				ja.add(json);
			}

		}
		
		if(list.size() > 0){
			Map<String, JSONObject> photoMap = exportPhotos(photoGets);
			
			for(int i=0;i<list.size();i++){
				JSONObject json = list.get(i);
				
				JSONArray feedbacks = json.getJSONArray("attachments");
				
				JSONArray newFeedbacks = new JSONArray();
				
				for(int j=0;j<feedbacks.size();j++){
					
					JSONObject newFeedback = new JSONObject();
					
					JSONObject feedback = feedbacks.getJSONObject(j);
					
					int type = feedback.getInt("type");
					
					String content = feedback.getString("content");
					
					newFeedback.put("type", type);
					
					newFeedback.put("content", content);
					
					if (type != 1){
						
						newFeedback.put("id", JSONNull.getInstance());
						
						newFeedback.put("extContent", JSONNull.getInstance());
					}
					else{
						
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
	
	private Map<String, JSONObject> exportPhotos(List<Get> gets) throws Exception{
		
		Map<String, JSONObject> photoMap = new HashMap<String, JSONObject>();
		
		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.photoTab));
		
		Result[] results = htab.get(gets);
		
		for (Result result : results){
			if (result.isEmpty()) {
				continue;
			}
			
			String rowkey = new String(result.getRow());
			
			byte[] data = result.getValue("data".getBytes(),"origin".getBytes());
			
			if(data == null || data.length==0){
				continue;
			}
			
			JSONObject attribute = JSONObject.fromObject(new String(result.getValue("data".getBytes(),
					"attribute".getBytes())));
			
			JSONObject extContent = new JSONObject();
			
			extContent.put("latitude", attribute.getDouble("a_latitude"));
			
			extContent.put("longitude", attribute.getDouble("a_longitude"));
			
			extContent.put("direction", attribute.getInt("a_direction"));
			
			extContent.put("shootDate", attribute.getString("a_shootDate"));
			
			extContent.put("deviceNum", attribute.getString("a_deviceNum"));
			
			String fileName = this.folderName + attribute.getString("a_fileName");
			
			photoMap.put(rowkey, extContent);
			
			FileUtils.makeSmallImage(data, fileName);
			
		}
		
		return photoMap;
		
	}

	/**
	 * 根据网格和时间导出tips
	 * 
	 * @param grids
	 *            网格数组 整型
	 * @param date
	 *            时间
	 * @param fileName
	 *            导出文件名
	 * @return 导出个数
	 * @throws Exception
	 */
	public int export(JSONArray grids, String date, String folderName, String fileName)
			throws Exception {

		int count = 0;
		
		if (! folderName.endsWith("/")){
			folderName += "/";
		}
		
		this.folderName = folderName;
		
		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);

		List<Get> gets = generateGets(grids, date);

		JSONArray ja = exportByGets(gets);

		for (int j = 0; j < ja.size(); j++) {
			pw.println(ja.getJSONObject(j).toString());

			count += 1;
		}

		pw.close();

		solrConn.closeConnection();

		return count;
	}

	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseAddress("192.168.3.156");
		JSONArray grids = new JSONArray();

		grids.add(60560304);

		TipsExporter exporter = new TipsExporter(
				"http://192.168.4.130:8081/solr/tips/");
		System.out.println(exporter.export(grids, "20150302010101","C:/Users/wangshishuai3966/Desktop"
				,"1.txt"));
		System.out.println("done");
	}
}
