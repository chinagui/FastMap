package com.navinfo.dataservice.FosEngine.export;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.geom.GeoTranslator;
import com.navinfo.dataservice.FosEngine.comm.util.GeohashUtils;
import com.navinfo.dataservice.FosEngine.comm.util.GridUtils;

public class ExportTips {

	public static int run(String wkt, String date, String fileName)
			throws Exception {

		int count = 0;

		PrintWriter pw = new PrintWriter(fileName);

		double mbr[] = GeoTranslator.getMBR(wkt);

		String startRowkey = GeoHash.geoHashStringWithCharacterPrecision(
				mbr[1], mbr[0], 12);

		String stopRowkey = GeoHash.geoHashStringWithCharacterPrecision(mbr[3],
				mbr[2], 12)+ "{";

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

				boolean flag = true;

				JSONObject json = new JSONObject();
				for (KeyValue kv : list) {
					
					String rowkey = new String(kv.key());
					
					if (!GeohashUtils.isWithin(rowkey, mbr[0], mbr[2], mbr[1], mbr[3])){
						flag=false;
						break;
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
						
						json.put("t_operateDate", lastDate);

						json.put("t_lifecycle", jo.getInt("t_lifecycle"));

						json.put("rowkey", new String(kv.key()));
					}

				}

				if (flag) {
					json.put("s_project", JSONNull.getInstance());

					json.put("t_status", 0);

					

					json.put("t_handler", 0);

					json.put("feedback", JSONNull.getInstance());

					pw.println(json.toString());

					count += 1;
				}
			}
		}
		pw.close();

		return count;
	}
	
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

	/**
	 * 根据网格和时间导出tips
	 * @param grids 网格数组 整型
	 * @param date 时间
	 * @param fileName 导出文件名
	 * @return 导出个数
	 * @throws Exception
	 */
	public static int run(JSONArray grids, String date, String fileName)
			throws Exception {

		int count = 0;

		PrintWriter pw = new PrintWriter(fileName);
		
		for(int i=0;i<grids.size();i++){
			String gridId = String.valueOf(grids.getInt(i));
			
			JSONArray ja = exportByGrid(gridId, date);
			
			for(int j=0;j<ja.size();j++){
				pw.println(ja.getJSONObject(j).toString());
				
				count+=1;
			}
				
		}
		
		pw.close();

		return count;
	}
	
	public static void main(String[] args) throws Exception {
		HBaseAddress.initHBaseClient("192.168.3.156");
		JSONArray grids=new JSONArray();
		
		grids.add(59567201);
		grids.add(59567202);
		grids.add(59567203);
		run(grids, "20150302010101", "C:/Users/wangshishuai3966/Desktop/1.txt");
		System.out.println("done");
	}
}
