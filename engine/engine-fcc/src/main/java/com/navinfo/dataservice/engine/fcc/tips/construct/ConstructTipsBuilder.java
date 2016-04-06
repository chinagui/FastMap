package com.navinfo.dataservice.engine.fcc.tips.construct;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

public class ConstructTipsBuilder {

	private static class Construct {
		public int sNodePid;

		public Geometry sNodeGeom;

		public Geometry eNodeGeom;

		public int eNodePid;

		public List<Integer> linkPids = new ArrayList<Integer>();

		public List<Geometry> linkGeoms = new ArrayList<Geometry>();

		public String time;

		public int level;
	}

	private static String sql = "with tmp1 as  (select link_pid,          decode(direct, 1, s_node_pid, 2, s_node_pid, e_node_pid) s_node_pid,          decode(direct, 1, e_node_pid, 2, e_node_pid, s_node_pid) e_node_pid     from rd_link a    where a.link_pid in          (select link_pid from rd_link_limit where type = 4)), tmp2 as  (select * from tmp1 where s_node_pid not in (select e_node_pid from tmp1)), tmp3 as  (select link_pid,          s_node_pid,          e_node_pid,          connect_by_root(link_pid) root_link_pid,          connect_by_isleaf isleaf,          level lvl     from tmp1   connect by nocycle s_node_pid = prior e_node_pid    start with link_pid in (select link_pid from tmp2)) select a.*,        b.geometry link_geom,        c.geometry s_point_geom,        d.geometry e_point_geom,        e.time_domain   from tmp3 a,        rd_link b,        rd_node c,        rd_node d,        rd_link_limit e  where a.link_pid = b.link_pid    and a.s_node_pid = c.node_pid    and a.e_node_pid = d.node_pid    and a.link_pid = e.link_pid    and e.type=4  order by root_link_pid, lvl desc";

	private static String type = "1514";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab)
			throws Exception {

		SolrBulkUpdater solrConn = new SolrBulkUpdater(
				TipsImportUtils.QueueSize, TipsImportUtils.ThreadCount);

		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		String date = sdf.format(new Date());

		List<Construct> tips = new ArrayList<Construct>();

		int currentRoot = 0;

		while (resultSet.next()) {
			num++;
			
			int rootLinkPid = resultSet.getInt("root_link_pid");

			int level = resultSet.getInt("lvl");

			int sNodePid = resultSet.getInt("s_node_pid");

			int eNodePid = resultSet.getInt("e_node_pid");

			int linkPid = resultSet.getInt("link_pid");

			int isLeaf = resultSet.getInt("isLeaf");

			String time = resultSet.getString("time_domain");

			STRUCT struct = (STRUCT) resultSet.getObject("s_point_geom");

			Geometry sNodeGeom = GeoTranslator.struct2Jts(struct);

			struct = (STRUCT) resultSet.getObject("e_point_geom");

			Geometry eNodeGeom = GeoTranslator.struct2Jts(struct);

			struct = (STRUCT) resultSet.getObject("link_geom");

			Geometry linkGeom = GeoTranslator.struct2Jts(struct);

			boolean found = false;

			if (rootLinkPid != currentRoot) {
				generateTips(tips, puts, date, solrConn);
				tips.clear();
				currentRoot = rootLinkPid;
			} else {
				if (isLeaf == 0) {
					for (Construct tip : tips) {
						if (tip.level != level + 1) {
							continue;
						}

						if (tip.sNodePid != eNodePid) {
							continue;
						}
						
						if (!StringUtils.isStringSame(tip.time, time)) {
							continue;
						}

						found = true;

						tip.sNodePid = sNodePid;

						tip.level = level;

						tip.linkPids.add(linkPid);

						tip.sNodeGeom = sNodeGeom;

						tip.linkGeoms.add(linkGeom);

						break;
					}
				}
			}

			if (!found) {

				Construct tip = new Construct();

				tip.sNodePid = sNodePid;

				tip.eNodePid = eNodePid;

				tip.level = level;

				tip.linkPids.add(linkPid);

				tip.sNodeGeom = sNodeGeom;

				tip.eNodeGeom = eNodeGeom;

				tip.linkGeoms.add(linkGeom);

				tip.time = time;

				tips.add(tip);
			}

			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();
				
			}
		}

		htab.put(puts);

		solrConn.commit();

		solrConn.close();

	}

	private static void generateTips(List<Construct> tips, List<Put> puts,
			String date, SolrBulkUpdater solrConn) throws Exception {
		if (tips.isEmpty()) {
			return;
		}

		for (Construct tip : tips) {

			Collections.reverse(tip.linkPids);

			Collections.reverse(tip.linkGeoms);

			JSONObject geometry = new JSONObject();

			geometry.put("g_guide", GeoTranslator.jts2Geojson(tip.sNodeGeom));
			
			geometry.put("g_location", connectLinks(tip.linkGeoms));

			JSONObject deep = new JSONObject();

			String time = tip.time;

			if (time != null) {
				deep.put("time", time);
			} else {
				deep.put("time", JSONNull.getInstance());
			}

			deep.put("gSLoc", GeoTranslator.jts2Geojson(tip.sNodeGeom));

			deep.put("gELoc", GeoTranslator.jts2Geojson(tip.eNodeGeom));

			JSONArray fArray = new JSONArray();

			for (int i = 0; i < tip.linkPids.size(); i++) {

				JSONObject fJson = new JSONObject();

				fJson.put("id", String.valueOf(tip.linkPids.get(i)));

				fJson.put("type", 1);

				String flag = "0";

				if (tip.linkPids.size() == 1) {
					flag = "1|2";
				} else if (i == 0) {
					flag = "1";
				} else if (i == (tip.linkPids.size() - 1)) {
					flag = "2";
				}

				fJson.put("flag", flag);

				fArray.add(fJson);
			}

			deep.put("f_array", fArray);

			String rowkey = TipsImportUtils.generateRowkey(
					String.valueOf(Collections.min(tip.linkPids)), type);

			String source = TipsImportUtils.generateSource(type);

			String track = TipsImportUtils.generateTrack(date);

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(),
					source.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(),
					track.getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), geometry
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
					.getBytes());

			puts.add(put);

			JSONObject solrIndexJson = TipsImportUtils.assembleSolrIndex(
					rowkey, 0, date, type, deep.toString(),
					geometry.getJSONObject("g_location"),
					geometry.getJSONObject("g_guide"));

			solrConn.addTips(solrIndexJson);
			
		}
	}

	private static JSONObject connectLinks(List<Geometry> geoms)
			throws ParseException {
		JSONObject json = new JSONObject();

		json.put("type", "LineString");

		Geometry geom = geoms.get(0);

		for (int i = 1; i < geoms.size(); i++) {
			geom = geom.union(geoms.get(i));
		}

		Coordinate[] cs = geom.getCoordinates();

		List<double[]> ps = new ArrayList<double[]>();

		for (Coordinate c : cs) {
			double[] p = new double[2];

			p[0] = c.x;

			p[1] = c.y;

			ps.add(p);
		}

		json.put("coordinates", ps);

		return json;
	}

}
