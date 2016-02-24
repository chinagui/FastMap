package com.navinfo.dataservice.FosEngine.tips.highway;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.FosEngine.tips.TipsImportUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.solr.core.SConnection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class HighwayTipsBuilder {

	private static String sql = "with tmp1 as  (select a.branch_pid,          b.arrow_code,          b.exit_num,       "
			+ "   c.names,          a.in_link_pid,          a.node_pid,          a.out_link_pid     from rd_branch a,   "
			+ "       (select *             from (select b.*,                         "
			+ " row_number() over(partition by branch_pid order by branch_type desc) ro                    "
			+ " from rd_branch_detail b                    where (b.branch_type in (0, 2) or                "
			+ "          (b.branch_type = 1 and b.pattern_code is not null)))            where ro = 1) b,     "
			+ "     (select b.detail_id,                  listagg(b.name, ',') within group(order by seq_num) names    "
			+ "         from rd_branch_name b            group by b.detail_id) c    where a.branch_pid = b.branch_pid     "
			+ " and b.detail_id = c.detail_id(+)), tmp2 as  (select in_link_pid,          node_pid,         "
			+ " listagg(branch_pid || '-' || arrow_code || '-' || exit_num || '-' ||                  names || '-' || out_link_pid,                  '^') within group(order by out_link_pid) info  "
			+ "   from tmp1    group by in_link_pid, node_pid) select a.*, b.direct, b.geometry link_geom,c.geometry point_geom "
			+ "  from tmp2 a, rd_link b,rd_node c  where a.in_link_pid = b.link_pid and a.node_pid = c.node_pid";

	private static String type = "1407";

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

			uniqId = resultSet.getString("node_pid");

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

	private static String generateGeometry(ResultSet resultSet)
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

		return geometry.toString();

	}

	private static String generateDeep(ResultSet resultSet) throws Exception {

		JSONObject deep = new JSONObject();

		JSONArray brID = new JSONArray();

		JSONObject in = new JSONObject();
		
		in.put("id", resultSet.getString("in_link_pid"));
		
		in.put("type", 1);
		
		deep.put("in", in);
		
		STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(new WKT().fromJGeometry(geom1));

		STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom2 = JGeometry.load(struct2);

		String pointWkt = new String(new WKT().fromJGeometry(geom2));
		
		double agl = DisplayUtils.calIncloudedAngle(linkWkt, getDirect(linkWkt,pointWkt));
		
		deep.put("agl", agl);
		
		String ptn = null;
		
		deep.put("ptn", JSONNull.getInstance());
		
		JSONArray info = new JSONArray();
		
		JSONArray o_array = new JSONArray();
		
		String[] branchs = resultSet.getString("info").split("\\^");
		
		int sq = 1;
		
		for(String branch : branchs){
			
			String[] splits = branch.split("-");
			
			JSONObject json = new JSONObject();
			
			json.put("id", splits[0]);
			
			json.put("sq", sq);
			
			brID.add(json);
			
			json = new JSONObject();
			
			json.put("sq", sq);
			
			json.put("arw", splits[1]);
			
			if ("".equals(splits[2])){
				json.put("exit", JSONNull.getInstance());
			}else{
				json.put("exit", splits[2]);
			}
			
			String names = splits[3];
			
			if ("".equals(names)){
				json.put("n_array", new int[]{});
			}else{
				json.put("n_array", names.split(","));
			}
			
			info.add(json);
			
			json = new JSONObject();
			
			json.put("sq", sq);
			
			json.put("arw", splits[1]);
			
			if ("".equals(splits[2])){
				json.put("exit", JSONNull.getInstance());
			}else{
				json.put("exit", splits[2]);
			}
			
			names = splits[3];
			
			if ("".equals(names)){
				json.put("n_array", new int[]{});
			}else{
				json.put("n_array", names.split(","));
			}
			
			JSONObject out = new JSONObject();
			
			out.put("id", splits[4]);
			
			out.put("type", 1);
			
			json.put("out", out);
			
			o_array.add(json);
			
			sq++;
		}
		
		deep.put("brID", brID);
		
		deep.put("info", info);
		
		deep.put("o_array", o_array);

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
	
	
	// 组装solr索引
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
