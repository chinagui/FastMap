package com.navinfo.dataservice.engine.fcc.tips.highway;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class HighwayTipsBuilder {

	private static String sql = "with tmp1 as  (select a.branch_pid,    b.arrow_code,    b.pattern_code,    b.exit_num,    c.names,    a.in_link_pid,    a.node_pid,    a.out_link_pid  from rd_branch a,    (select *    from (select b.*,        row_number() over(partition by branch_pid order by branch_type desc) ro      from rd_branch_detail b     where (b.branch_type in (0, 2) or        (b.branch_type = 1 and b.pattern_code is not null)))   where ro = 1) b,    (select b.detail_id,      listagg(b.name, ',') within group(order by seq_num) names    from rd_branch_name b   group by b.detail_id) c where a.branch_pid = b.branch_pid   and b.detail_id = c.detail_id(+)), tmp2 as  (select in_link_pid,    node_pid,     listagg(branch_pid || '-' || arrow_code || '-' || exit_num || '-' ||      names || '-' || out_link_pid || '-' || pattern_code,      '^') within group(order by out_link_pid) info  from tmp1 group by in_link_pid, node_pid) select a.*, b.direct, b.geometry link_geom, c.geometry point_geom   from tmp2 a, rd_link b, rd_node c  where a.in_link_pid = b.link_pid and a.node_pid = c.node_pid";
	private static String type = "1407";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab)
			throws Exception {
		
		SolrBulkUpdater solrConn = new SolrBulkUpdater(TipsImportUtils.QueueSize,TipsImportUtils.ThreadCount);
		Statement stmt = null;

		ResultSet resultSet = null;
		try{
			stmt = fmgdbConn.createStatement();
	
			resultSet = stmt.executeQuery(sql);
	
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
	
				String feedback = TipsImportUtils.generateFeedback();
				
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
				
				put.addColumn("data".getBytes(), "feedback".getBytes(), feedback.getBytes());
	
				puts.add(put);
				
				JSONObject solrIndexJson = TipsImportUtils.assembleSolrIndex(rowkey, 0, date, type, deep.toString(), geometry.getJSONObject("g_location"), geometry.getJSONObject("g_guide"), "[]");
	
				solrConn.addTips(solrIndexJson);
	
				if (num % 5000 == 0) {
					htab.put(puts);
	
					puts.clear();
	
				}
			}
	
			htab.put(puts);
			
			solrConn.commit();
			
			solrConn.close();
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(stmt);
		}
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
				.getTipsPointPos(linkWkt, pointWkt, 3);

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
		
		double agl = DisplayUtils.calIncloudedAngle(linkWkt, DisplayUtils.getDirect(linkWkt,pointWkt));
		
		deep.put("agl", agl);
		
		deep.put("ptn", JSONNull.getInstance());
		
		JSONArray info = new JSONArray();
		
		JSONArray o_array = new JSONArray();
		
		String[] branchs = resultSet.getString("info").split("\\^");
		
		int sq = 1;
		
		for(String branch : branchs){
			
			String[] splits = branch.split("-");
			
			deep.put("ptn", splits[5]);
			
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
	
	
}
