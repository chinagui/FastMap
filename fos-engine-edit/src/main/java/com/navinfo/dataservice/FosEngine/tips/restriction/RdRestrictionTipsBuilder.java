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
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.timedomain.TimeDecoder;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RdRestrictionTipsBuilder {
	
	private static String sql = "select  a.*,b.geometry link_geom,c.geometry point_geom  from "
			+ "( select  a.pid,a.in_link_pid,a.node_pid,b.detail   from rd_restriction a,"
			+ "( select  listagg(a.restric_info||','||a.flag||','||a.out_link_pid||','||b.time_domain,'-')  within group(order by a.out_link_pid) detail,"
			+ "restric_pid  from rd_restriction_detail a,rd_restriction_condition b "
			+ "where a.detail_id = b.detail_id(+) group by restric_pid) b "
			+ "where a.pid = b.restric_pid) a,rd_link b,rd_node c where a.in_link_pid = b.link_pid and a.node_pid = c.node_pid";

	private static String type = "1302";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,String solrUrl)
			throws Exception {

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

			String rowkey = TipsImportUtils.generateRowkey(uniqId, type);

			String source = TipsImportUtils.generateSource(type);

			String track = TipsImportUtils.generateTrack(date);

			JSONObject geometry = generateGeometry(resultSet);

			String deep = generateDeep(resultSet);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(),
					source.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(),
					track.getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(),
					geometry.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.getBytes());

			puts.add(put);
			
			JSONObject solrIndexJson = TipsImportUtils.assembleSolrIndex(rowkey, 0, date, type, deep.toString(), geometry.getJSONObject("g_location"), geometry.getJSONObject("g_guide"));

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

	private static JSONObject generateGeometry(ResultSet resultSet)
			throws Exception {

		STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(new WKT().fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String pointWkt = new String(new WKT().fromJGeometry(geom2));

		double[][] point = DisplayUtils
				.getLinkPointPos(linkWkt, pointWkt, 1, 0);

		JSONObject json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point[1]);

		JSONObject geometry = new JSONObject();

		geometry.put("g_location", json);

		json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point[0]);

		geometry.put("g_guide", json);

		return geometry;
	}

	private static String generateDeep(ResultSet resultSet) throws Exception {

		JSONObject deep = new JSONObject();
		
		deep.put("id", resultSet.getString("pid"));
		
		JSONObject in = new JSONObject();
		
		in.put("id", resultSet.getString("in_link_pid"));
		
		in.put("type", 1);
		
		deep.put("in", in);
		
		JSONArray info = new JSONArray();
		
		JSONArray o_array = new JSONArray();
		
		String[] details = resultSet.getString("detail").split("-");
		
		int sq = 1;
		
		for(String detail : details){
			
			String[] splits = detail.split(",");
			
			int restricInfo = Integer.parseInt(splits[0]);
			
			int flag = Integer.parseInt(splits[1]);
			
			String outLinkPid = splits[2];
			
			JSONObject infoJson = new JSONObject();
			
			infoJson.put("info", restricInfo);
			
			infoJson.put("flag", flag);
			
			infoJson.put("sq", sq);
			
			info.add(infoJson);
			
			infoJson = new JSONObject();
			
			infoJson.put("oInfo", restricInfo);
			
			infoJson.put("flag", flag);
			
			infoJson.put("sq", sq);
			
			JSONObject outJson = new JSONObject();
			
			outJson.put("id", outLinkPid);
			
			outJson.put("type", 1);
			
			infoJson.put("out", new JSONObject[]{outJson});
			
			if (splits.length == 3){
				infoJson.put("time", JSONNull.getInstance());
			}else{
				
				TimeDecoder decoder = new TimeDecoder();
				
				infoJson.put("time",  decoder.decode(splits[3]));
			}
			
			o_array.add(infoJson);
			
			sq++;
		}
		
		deep.put("info", info);
		
		deep.put("o_array", o_array);
		
		STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(new WKT().fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String pointWkt = new String(new WKT().fromJGeometry(geom2));
		
		int direct = getDirect(linkWkt, pointWkt);
		
		deep.put("agl", DisplayUtils.calIncloudedAngle(linkWkt, direct));
		
		return deep.toString();
	}
	
	private static int getDirect(String linkWkt,String pointWkt) throws ParseException{
		
		int direct = 2;
		
		Geometry link = new WKTReader().read(linkWkt);
		
		Geometry point = new WKTReader().read(pointWkt);
		
		Coordinate[] csLink = link.getCoordinates();
		
		Coordinate cPoint = point.getCoordinate();
		
		if (csLink[0].x != cPoint.x || csLink[1].y != cPoint.y){
			direct = 3;
		}
		
		return direct;
	}

}
