package com.navinfo.dataservice.FosEngine.tips.restriction;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.comm.geom.Geojson;
import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;

public class RdRestrictionTipsBuilder {

	private static String sql = "";
	
	private static String type = "1510";
	
	/**
	 * 导入入口程序块
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab) throws Exception{
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;
		
		double[] lonlat = new double[2];
		
		String uniqId = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;
			
			String rowkey = TipsImportUtils.generateRowkey(lonlat, uniqId, type);
			
			String source = TipsImportUtils.generateSource(type);
			
			String track = TipsImportUtils.generateTrack(date);
			
			String geometry = generateGeometry(resultSet);
			
			String deep = generateDeep(resultSet);
			
			Put put = new Put(rowkey.getBytes());
			
			put.addColumn("data".getBytes(), "source".getBytes(),source.getBytes());
			
			put.addColumn("data".getBytes(), "track".getBytes(),track.getBytes());
			
			put.addColumn("data".getBytes(), "geometry".getBytes(),geometry.getBytes());
			
			put.addColumn("data".getBytes(), "deep".getBytes(),deep.getBytes());
			
			puts.add(put);
			
			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

			}
		}

		htab.put(puts);
		
	}
	
	private static String generateGeometry(ResultSet resultSet) throws Exception{
		double[] point = null;

		JSONObject geojson = Geojson.point2Geojson(point);
		
		JSONObject geometryjson = new JSONObject();
		geometryjson.put("g_location", geojson);
		geometryjson.put("g_guide", geojson);
		
		return geometryjson.toString();
	}
	
	private static String generateDeep(ResultSet resultSet) throws Exception{
		
		JSONObject dpattrjson = new JSONObject();

		JSONArray residarary = new JSONArray();
		
		String residstr = resultSet.getString("resid");

		String[] items = residstr.split("-");

		for (String item : items) {
			String[] resid = item.split("\\|");

			JSONObject residjson = new JSONObject();

			residjson.put("id", Integer.valueOf(resid[0]));
			residjson.put("type", Integer.valueOf(resid[1]));

			residarary.add(residjson);
		}

		dpattrjson.put("resID", residarary);

		JSONObject injson = new JSONObject();
		
		String linkpid = resultSet.getString("link_pid");

		injson.put("id", linkpid);
		injson.put("type", 1);
		dpattrjson.put("in", injson);

		JSONArray infoarary = new JSONArray();

		JSONArray oarary = new JSONArray();
		
		String outstr = resultSet.getString("out_array");

		String[] oitems = outstr.split("-");

		int sq = 0;

		for (String item : oitems) {
			String[] out = item.split("\\|");
			JSONObject ojson = new JSONObject();

			JSONArray outarray = new JSONArray();

			JSONObject outjson = new JSONObject();

			outjson.put("id", out[0]);
			outjson.put("type", 1);

			outarray.add(outjson);

			ojson.put("out", outarray);

			int info = Integer.valueOf(out[1]);

			int flag = Integer.valueOf(out[2]);

			int vt = Integer.valueOf(out[3]);

			ojson.put("oInfo", info);
			ojson.put("flag", flag);
			ojson.put("vt", vt);
			ojson.put("sq", sq);

			JSONObject infojson = new JSONObject();

			infojson.put("info", info);
			infojson.put("flag", flag);
			infojson.put("vt", vt);
			infojson.put("sq", sq);

			sq += 1;

			infoarary.add(infojson);

			JSONArray carary = new JSONArray();
			if (!"^".equals(out[4])) {
				String[] cstrs = out[4].split("_");

				for (String cstr : cstrs) {

					JSONObject cjson = new JSONObject();

					String[] ccstrs = cstr.split("@");

					if (" ".equals(ccstrs[0])) {
						cjson.put("time", JSONNull.getInstance());
					} else {

						cjson.put("time", ccstrs[0]);
					}

					if (ccstrs.length > 1) {
						JSONObject truckjson = new JSONObject();

						truckjson.put("tra", Integer.valueOf(ccstrs[1]));
						truckjson.put("w", Double.valueOf(ccstrs[2]));
						truckjson.put("aLd", Double.valueOf(ccstrs[3]));
						truckjson.put("aCt", Integer.valueOf(ccstrs[4]));
						truckjson.put("rOut", Integer.valueOf(ccstrs[5]));

						cjson.put("truck", truckjson);
					} else {
						cjson.put("truck", JSONObject.fromObject(null));
					}

					carary.add(cjson);
				}
			}
			ojson.put("c_array", carary);

			oarary.add(ojson);
		}

		dpattrjson.put("info", infoarary);

		dpattrjson.put("o_array", oarary);
		
		return dpattrjson.toString();
	}
	
}
