package com.navinfo.dataservice.FosEngine.export;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.util.GridUtils;
import com.navinfo.dataservice.solr.core.SConnection;

public class ExportTips {

	private static JSONArray exportByGrid(String gridId, String date) throws Exception{
		
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
					
					double lonlat[] = GridUtils.geohash2Lonlat(rowkey.substring(0, 12));
					
					if (!flag && !gridId.equals(GridUtils.location2Grid(lonlat[0], lonlat[1]))){
						flag=false;
						break;
					}
					else{
						flag=true;
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

						JSONArray tTrackInfo = jo.getJSONArray("t_trackInfo");

						String lastDate = tTrackInfo.getJSONObject(
								tTrackInfo.size() - 1).getString("date");

						if (date.compareTo(lastDate) > 0) {
							flag = false;
							break;
						}

						json.put("t_lifecycle", jo.getInt("t_lifecycle"));
						
						json.put("t_operateDate", lastDate);

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
	
	private static List<Get> generateGets(JSONArray grids, String date, String solrUrl) throws Exception{
		List<Get> gets = new ArrayList<Get>();
		
		Set<String> set = new HashSet<String>();
		
		SConnection conn = new SConnection(solrUrl);
		
		for(int i=0; i<grids.size(); i++){
			String gridId = grids.getString(i);
			
			String wkt = GridUtils.grid2Wkt(gridId);
			
			List<String> rowkeys = conn.queryTipsMobile(wkt, date);
			
			for(String rowkey:rowkeys){
				if (set.contains(rowkey)){
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
	
	private static JSONArray exportByGets(List<Get> gets) throws Exception{
		
		JSONArray ja = new JSONArray();
		
		Connection hbaseConn = HBaseAddress.getHBaseConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf("tips"));
		
		Result[] results = htab.get(gets);

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

			JSONArray tTrackInfo = trackjson.getJSONArray("t_trackInfo");

			json.put("t_lifecycle", trackjson.getInt("t_lifecycle"));
			
			json.put("t_command", trackjson.getInt("t_command"));
			
			String lastDate = tTrackInfo.getJSONObject(
					tTrackInfo.size() - 1).getString("date");
			
			json.put("t_operateDate", lastDate);

			json.put("t_status", 0);

			json.put("t_handler", 0);
			
			if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
				JSONObject feedback = JSONObject.fromObject(new String(result
						.getValue("data".getBytes(), "feedback".getBytes())));

				json.put("feedback", feedback.getJSONArray("f_array"));
			} else {
				json.put("feedback", new JSONArray());
			}

			ja.add(json);

		}
		
		return ja;
	}

	/**
	 * 根据网格和时间导出tips
	 * @param grids 网格数组 整型
	 * @param date 时间
	 * @param fileName 导出文件名
	 * @return 导出个数
	 * @throws Exception
	 */
	public static int run(JSONArray grids, String date, String fileName, String solrUrl)
			throws Exception {

		int count = 0;

		PrintWriter pw = new PrintWriter(fileName);
		
//		for(int i=0;i<grids.size();i++){
//			String gridId = String.valueOf(grids.getInt(i));
//			
//			JSONArray ja = exportByGrid(gridId, date);
//			
//			for(int j=0;j<ja.size();j++){
//				pw.println(ja.getJSONObject(j).toString());
//				
//				count+=1;
//			}
//				
//		}
		
		List<Get> gets = generateGets(grids, date, solrUrl);
		
		JSONArray ja = exportByGets(gets);
		
		for(int j=0;j<ja.size();j++){
			pw.println(ja.getJSONObject(j).toString());
			
			count+=1;
		}
		
		pw.close();

		return count;
	}
	
	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseAddress("192.168.3.156");
		JSONArray grids=new JSONArray();
		
		grids.add(59567201);
		grids.add(59567202);
		System.out.println(run(grids, "20150302010101", "C:/Users/wangshishuai3966/Desktop/1.txt","http://192.168.4.130:8081/solr/tips/"));
		System.out.println("done");
	}
}
