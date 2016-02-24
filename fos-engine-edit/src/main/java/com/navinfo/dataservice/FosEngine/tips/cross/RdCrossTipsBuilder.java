package com.navinfo.dataservice.FosEngine.tips.cross;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;

public class RdCrossTipsBuilder {
	
	private static final WKT wkt = new WKT();

	private static String sql = "with tmp1 as  (select a.pid,          a.node_pid,          b.geometry geom,          row_number() over(partition by a.node_pid order by b.link_pid) ro     from rd_cross_node a, rd_link b    where a.is_main = 1      and a.node_pid in (b.s_node_pid, b.e_node_pid)), tmp2 as  (select a.pid, a.node_pid, a.geom geom1, b.geom geom2     from tmp1 a, tmp1 b    where a.pid = b.pid      and a.node_pid = b.node_pid      and a.ro = 1      and b.ro = 2) select a.pid, a.node_pid, a.geom1, a.geom2, b.geometry point_geom, c.name   from tmp2 a, rd_node b, rd_cross_name c  where a.node_pid = b.node_pid    and a.pid = c.pid(+)    and c.lang_code(+) in ('CHI', 'CHT')";
	
	private static String type = "1704";
	
	/**
	 * 导入入口程序块
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,String solrUrl) throws Exception{
		
		SConnection solrConn = new SConnection(solrUrl,5000);
		
		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(1000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;
		
		String uniqId = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = sdf.format(new Date());

		while (resultSet.next()) {
			num++;
			uniqId = resultSet.getString("pid");
			
			String rowkey = TipsImportUtils.generateRowkey(uniqId, type);
			
			String source = TipsImportUtils.generateSource(type);
			
			String track = TipsImportUtils.generateTrack(date);
			
			String gLocation = generateGeometry(resultSet);
			
			STRUCT structPonit = (STRUCT) resultSet.getObject("point_geom");

			JGeometry geomPoint = JGeometry.load(structPonit);

			String pointWkt = new String(wkt.fromJGeometry(geomPoint));
			
			JSONObject gGuide = Geojson.wkt2Geojson(pointWkt);
			
			JSONObject geometry = new JSONObject();
			
			geometry.put("g_location", gLocation);
			
			geometry.put("g_guide", gGuide);
			
			String deep = generateDeep(resultSet);
			
			Put put = new Put(rowkey.getBytes());
			
			put.addColumn("data".getBytes(), "source".getBytes(),source.getBytes());
			
			put.addColumn("data".getBytes(), "track".getBytes(),track.getBytes());
			
			put.addColumn("data".getBytes(), "geometry".getBytes(),geometry.toString().getBytes());
			
			put.addColumn("data".getBytes(), "deep".getBytes(),deep.getBytes());
			
			puts.add(put);
			
			JSONObject solrIndexJson = assembleSolrIndex(rowkey, JSONObject.fromObject(geometry), 0, date, type, deep);
			
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
	
	private static String generateGeometry(ResultSet resultSet ) throws Exception{

		STRUCT struct1 = (STRUCT) resultSet.getObject("geom1");

		JGeometry geom1 = JGeometry.load(struct1);

		String geom1Wkt = new String(wkt.fromJGeometry(geom1));
		
		STRUCT struct2 = (STRUCT) resultSet.getObject("geom2");

		JGeometry geom2 = JGeometry.load(struct2);

		String geom2Wkt = new String(wkt.fromJGeometry(geom2));
		
		STRUCT structPonit = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geomPoint = JGeometry.load(structPonit);

		String pointWkt = new String(wkt.fromJGeometry(geomPoint));
		
		double[] point = DisplayUtils.getCrossPoint(geom1Wkt, geom2Wkt, pointWkt);
		
		JSONObject json = new JSONObject();
		
		json.put("type", "Point");
		
		json.put("coordinates", point);
		
		return json.toString();
	}
	
	private static String generateDeep(ResultSet resultSet) throws SQLException{
		
		int crossPid = resultSet.getInt("pid");
		
		int nodePid = resultSet.getInt("node_pid");
		
		String crossName = resultSet.getString("name");
		
		JSONObject json = new JSONObject();
		
		json.put("id", String.valueOf(crossPid));
		
		JSONObject jsonF = new JSONObject();
		
		jsonF.put("id", String.valueOf(nodePid));
		
		jsonF.put("type", 1);
		
		json.put("f", jsonF);
		
		if(crossName == null){
			json.put("name", JSONNull.getInstance());
		}
		else{
			json.put("name", crossName);
		}
		
		return json.toString();
	}
	
	//组装solr索引
	private static JSONObject assembleSolrIndex(String rowkey, JSONObject geom,
			int stage, String date, String type, String deep) throws Exception {
		JSONObject json = new JSONObject();

		json.put("id", rowkey);

		json.put("stage", stage);

		json.put("date", date);

		json.put("t_lifecycle", 0);

		json.put("t_command", 0);

		json.put("handler", 0);

		json.put("s_sourceType", type);

		json.put("s_sourceCode", 11);

		JSONObject geojson = geom.getJSONObject("g_location");

		json.put("g_location", geojson);

		json.put("g_guide", geom.getJSONObject("g_guide"));

		json.put("wkt",
				GeoTranslator.jts2Wkt(GeoTranslator.geojson2Jts(geojson)));
		
		json.put("deep", deep);

		return json;
	}
	
}
