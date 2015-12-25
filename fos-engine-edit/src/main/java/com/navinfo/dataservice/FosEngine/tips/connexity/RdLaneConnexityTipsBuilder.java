package com.navinfo.dataservice.FosEngine.tips.connexity;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RdLaneConnexityTipsBuilder {

	private static final WKT wkt = new WKT();

	private static final WKTReader wktReader = new WKTReader();
	
	private static String sql = "select a.*,b.geometry link_geom,c.geometry point_geom  from "
			+ "( select a.pid,a.in_link_pid,a.node_pid,a.lane_info, a.lane_num,  b.topos from rd_lane_connexity a,"
			+ "( select  listagg( out_link_pid||','||in_lane_info||','||bus_lane_info||','||reach_dir,'-') within group(order by out_link_pid) topos,"
			+ "connexity_pid  from rd_lane_topology group by connexity_pid) b  where a.pid = b.connexity_pid ) a,rd_link b,rd_node c "
			+ " where a.in_link_pid = b.link_pid and a.node_pid = c.node_pid";

	private static String type = "1301";

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

			String geometry = generateGeometry(resultSet);

			String deep = generateDeep(resultSet);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(),
					source.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(),
					track.getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(),
					geometry.getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.getBytes());

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

	private static String generateGeometry(ResultSet resultSet)
			throws Exception {

		STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(wkt.fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String pointWkt = new String(wkt.fromJGeometry(geom2));

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

		return geometry.toString();
	}
	
	private static Map<Integer,JSONArray> parseLaneinfo(int laneNum,String topos){
		Map<Integer,JSONArray> map = new HashMap<Integer,JSONArray>();
		
		for(int i=1;i<=laneNum;i++){
			map.put(i, new JSONArray());
		}
		
		String[] topoSplits = topos.split("-");
		
		for(String topo : topoSplits){
			
			String[] splits = topo.split(",");
			
			String outLinkPid = splits[0];
			
			int inLaneInfo = Integer.parseInt(splits[1]);
			
			int busLaneInfo = Integer.parseInt(splits[2]);
			
			int reachDir = Integer.parseInt(splits[3]);
			
			JSONObject json = new JSONObject();
			
			json.put("outLinkPid", outLinkPid);
			
			json.put("inLaneInfo", inLaneInfo);
			
			json.put("busLaneInfo", busLaneInfo);
			
			json.put("reachDir", reachDir);
			
			for(int i=1;i<=laneNum;i++){
				
				String bin16 = convertLaneinfoTo16Bin(inLaneInfo);
				
				if (bin16.charAt(i-1) == '1'){
					JSONObject infoJson = JSONObject.fromObject(json.toString());
					
					bin16 = convertLaneinfoTo16Bin(inLaneInfo);
					
					if (bin16.charAt(i-1) == '1'){
						infoJson.put("isBus", 1);
					}else{
						infoJson.put("isBus", 0);
					}
					
					map.get(i).add(infoJson);
				}
			}
			
			
			
		}
		
		return map;
	}
	
	private static String convertLaneinfoTo16Bin(int laneInfo){
		String bin16 = null;
		
		bin16 = Integer.toBinaryString(laneInfo);
		
		for(int i=0;i<16-bin16.length();i++){
			bin16 = "0" + bin16;
		}
		
		
		return bin16;
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
		
		int laneNum = resultSet.getInt("lane_num");
		
		String topos = resultSet.getString("topos");
		
		Map<Integer,JSONArray> map = parseLaneinfo(laneNum,topos);
		
		String[] splitsLaneInfo = resultSet.getString("lane_info").split(",");
		
		for(int i=0;i<splitsLaneInfo.length;i++){
			
			JSONObject infoJson = new JSONObject();
			
			JSONObject oarrayJson = new JSONObject();
			
			infoJson.put("sq", i + 1);
			
			oarrayJson.put("sq", i + 1);
			
			if (splitsLaneInfo[i].indexOf("[")>=0){
				infoJson.put("ext", 1);
				oarrayJson.put("ext", 1);
			}else{
				infoJson.put("ext", 0);
				oarrayJson.put("ext", 0);
			}
			
			String laneinfo = splitsLaneInfo[i];
			
			if (laneinfo.indexOf("[")>=0){
				laneinfo = laneinfo.replace("[", "").replace("]", "");
			}
			
			if (laneinfo.indexOf("<")>0){
				infoJson.put("arwG", laneinfo.substring(0, laneinfo.indexOf("<")));
				
				infoJson.put("arwB", laneinfo.substring(laneinfo.indexOf("<") + 1, laneinfo.indexOf(">")));
			}else{
				infoJson.put("arwG", laneinfo);
				
				infoJson.put("arwB", JSONNull.getInstance());
			}
			
			info.add(infoJson);
			
			JSONArray mapArray = map.get(i+1);
			
			JSONArray d_array = new JSONArray();
			
			for(int j=0;j<mapArray.size();j++){
				JSONObject dJson = new JSONObject();
				
				JSONObject outJson = new JSONObject();
				
				outJson.put("id", mapArray.getJSONObject(j).getString("outLinkPid"));
				
				outJson.put("type", 1);
				
				dJson.put("out", new JSONObject[]{outJson});
				
				dJson.put("arw", mapArray.getJSONObject(j).getString("reachDir"));
				
				dJson.put("vt", mapArray.getJSONObject(j).getString("isBus"));
				
				d_array.add(dJson);
			}
			
			oarrayJson.put("d_array", d_array);
			
			o_array.add(oarrayJson);
			
		}
		
		deep.put("info", info);
		
		deep.put("o_array", o_array);

		STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(wkt.fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String pointWkt = new String(wkt.fromJGeometry(geom2));
		
		int direct = getDirect(linkWkt, pointWkt);
		
		deep.put("agl", DisplayUtils.calIncloudedAngle(linkWkt, direct));
		
		return deep.toString();
	}
	
	private static int getDirect(String linkWkt,String pointWkt) throws ParseException{
		
		int direct = 2;
		
		Geometry link = wktReader.read(linkWkt);
		
		Geometry point = wktReader.read(pointWkt);
		
		Coordinate[] csLink = link.getCoordinates();
		
		Coordinate cPoint = point.getCoordinate();
		
		if (csLink[0].x != cPoint.x || csLink[1].y != cPoint.y){
			direct = 3;
		}
		
		return direct;
	}
	
	// 组装solr索引
	private static JSONObject assembleSolrIndex(String rowkey, String geom,
			int stage) {
		JSONObject json = new JSONObject();

		json.put("i", rowkey);

		json.put("g", geom);

		json.put("m", "{\"a\":\"" + stage + "\"}");

		return json;
	}

}
