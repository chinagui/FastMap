package com.navinfo.dataservice.FosEngine.tips.speedLimit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;

public class RdSpeedLimitTipsBuilder {
	
	private static final WKT wkt = new WKT();

	private static String sql = "select a.pid,a.link_pid,a.tollgate_flag,a.direct,a.speed_value, " +
			"a.speed_flag,a.capture_flag,a.limit_src," +
			"a.geometry point_geom,b.geometry link_geom  " +
			"from rd_speedlimit a,rd_link b where a.link_pid = b.link_pid";
	
	private static String type = "1101";
	
	/**
	 * 导入入口程序块
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,String solrUrl) throws Exception{
		
		SConnection solrConn = new SConnection(solrUrl,5000);
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;
		
		String uniqId = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;
			
			uniqId = resultSet.getString("pid");
			
			String rowkey = TipsImportUtils.generateRowkey( uniqId, type);
			
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
			
			JSONObject solrIndexJson = assembleSolrIndex(rowkey, geometry, 0);
			
			solrConn.addTips(solrIndexJson);
			
			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

			}
		}

		htab.put(puts);
		
		solrConn.persistentData();
		
		solrConn.closeConnection();
		
	}
	
	private static String generateGeometry(ResultSet resultSet) throws Exception{
		
		STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String pointWkt = new String(wkt.fromJGeometry(geom1));
		
		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String linkWkt = new String(wkt.fromJGeometry(geom2));
		
		int direct = resultSet.getInt("direct");
		
		double[] point = DisplayUtils.calSpeedLimitPos(linkWkt, pointWkt, direct);
		
		JSONObject json = new JSONObject();
		
		json.put("type", "Point");
		
		json.put("coordinates", point);
		
		JSONObject geometry = new JSONObject();
		
		geometry.put("g_location", json);
		
		geometry.put("g_guide", Geojson.wkt2Geojson(pointWkt));
		
		return geometry.toString();
	}
	
	private static String generateDeep(ResultSet resultSet) throws Exception{
		
		JSONObject jsonDeep = new JSONObject();
		
		jsonDeep.put("tp", 1);
		
		jsonDeep.put("id", resultSet.getString("pid"));
		
		JSONObject jsonF = new JSONObject();
		
		jsonF.put("id", resultSet.getString("link_pid"));
		
		jsonF.put("type", 1);
		
		jsonDeep.put("f", jsonF);
		
		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String linkWkt = new String(wkt.fromJGeometry(geom2));
		
		jsonDeep.put("agl",DisplayUtils.calIncloudedAngle(linkWkt, resultSet.getInt("direct")));
		
		jsonDeep.put("toll", resultSet.getInt("tollgate_flag"));

		jsonDeep.put("rdDir", resultSet.getInt("direct"));
		
		jsonDeep.put("value", resultSet.getInt("speed_value"));
		
		jsonDeep.put("se", resultSet.getInt("speed_flag"));
		
		jsonDeep.put("flag", resultSet.getInt("capture_flag"));
		
		jsonDeep.put("src", resultSet.getInt("limit_src"));
		
		return jsonDeep.toString();
	}
	
	//组装solr索引
		private static JSONObject assembleSolrIndex(String rowkey,String geom,int stage){
			JSONObject json = new JSONObject();
			
			json.put("i", rowkey);
			
			json.put("g", geom);
			
			json.put("m", "{\"a\":\""+stage+"\"}");
			
			return json;
		}
	
}
