package com.navinfo.dataservice.FosEngine.tips.RdRestriction;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.sql.STRUCT;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.FosEngine.comm.constant.BusinessConstant;
import com.navinfo.dataservice.FosEngine.comm.geom.Geojson;
import com.navinfo.dataservice.FosEngine.comm.mercator.MercatorProjection;
import com.navinfo.dataservice.FosEngine.comm.service.ProgressService;

/**
 * 交限Tips创建类
 */
public class RdRestrictionTipsBuilder {

	private static Logger logger = Logger
			.getLogger(RdRestrictionTipsBuilder.class);

	/**
	 * 临时保存Tips的显示坐标 key为进入线pid+进入点pid value为显示坐标经纬度
	 */
	private static Map<String, double[]> pointMap = new HashMap<String, double[]>();

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void run(java.sql.Connection fmgdbConn,
			java.sql.Connection pmConn, Connection hbaseConn, String uuid,
			double scale) throws IOException, ClassNotFoundException,
			SQLException {

		try {
			createTabIfNotExists(hbaseConn, "tips");

			Table htab = hbaseConn.getTable(TableName.valueOf("tips"));

			ProgressService progressManager = new ProgressService(pmConn, uuid);

			importTips(fmgdbConn, htab, progressManager, scale);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化
	 * 
	 * @param connection
	 * @param tabName
	 * @throws IOException
	 */
	public static void createTabIfNotExists(Connection connection,
			String tabName) throws IOException {

		Admin admin = connection.getAdmin();

		TableName tableName = TableName.valueOf(tabName);

		if (!admin.tableExists(tableName)) {
			HTableDescriptor htd = new HTableDescriptor(tableName);

			HColumnDescriptor hcd = new HColumnDescriptor("data");

			htd.addFamily(hcd);

			admin.createTable(htd);
		}
	}

	/**
	 * 获取交限总数
	 * 
	 * @param conn
	 * @param tabName
	 * @return
	 * @throws SQLException
	 */
	public static int getRestrictNum(java.sql.Connection conn, String tabName)
			throws SQLException {
		int num = 0;

		Statement stmt = conn.createStatement();

		String sql = "select count(*) ct from " + tabName;

		ResultSet rs = stmt.executeQuery(sql);

		rs.next();

		num = rs.getInt("ct");

		return num;
	}

	/**
	 * 导入tips主方法
	 * 
	 * @param fmgdbConn
	 * @param htab
	 * @param progressManager
	 * @param scale
	 * @throws SQLException
	 * @throws GeometryExceptionWithContext
	 * @throws IOException
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab,
			ProgressService progressManager, double scale) throws SQLException,
			GeometryExceptionWithContext, IOException {

		prepareData(fmgdbConn, progressManager, scale);

		// 查询交限总数
		int restrictNum = getRestrictNum(fmgdbConn, "restrict_tmp1");

		String sql = "select a.link_pid,a.node_pid,a.resid,b.out_array "
				+ "from restrict_tmp1 a,restrict_tmp2 b " + "where "
				+ "a.link_pid = b.in_link_pid and a.node_pid = b.node_pid ";

		Statement stmt = fmgdbConn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);

		List<Put> puts = new ArrayList<Put>();

		int num = 0;

		while (resultSet.next()) {
			num++;

			insertTips(puts, resultSet);

			if (num % 5000 == 0) {
				htab.put(puts);

				puts.clear();

				double progress = ((double) num / restrictNum / 2 + 0.5) * 100;

				progressManager.updateProgress("完成度:"
						+ (int) (progress * scale) + "%");
			}
		}

		htab.put(puts);

		puts.clear();

		htab.close();

		progressManager.updateProgress("完成度:" + (int) (100 * scale) + "%");
	}

	/**
	 * 解析oracle数据并组装tips
	 * 
	 * @param puts
	 * @param resultSet
	 * @throws SQLException
	 */
	public static void insertTips(List<Put> puts, ResultSet resultSet)
			throws SQLException {
		String linkpid = resultSet.getString("link_pid");

		int nodePid = resultSet.getInt("node_pid");

		String residstr = resultSet.getString("resid");

		String outstr = resultSet.getString("out_array");

		double[] point = pointMap.get(linkpid + "," + nodePid);

		double lon = point[0];

		double lat = point[1];

		JSONObject geojson = Geojson.point2Geojson(point);

		String rowkey = GeoHash.geoHashStringWithCharacterPrecision(lat, lon,
				12)
				+ BusinessConstant.tipsRdRestriction
				+ String.format("%010d", Integer.parseInt(linkpid))
				+ String.format("%010d", nodePid);

		Put put = new Put(rowkey.getBytes());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		long date = Long.valueOf(sdf.format(new Date()));

		JSONObject geometryjson = new JSONObject();
		geometryjson.put("g_location", geojson);
		geometryjson.put("g_guide", geojson);

		put.addColumn("data".getBytes(), "geometry".getBytes(), geometryjson
				.toString().getBytes());

		JSONObject sourcejson = new JSONObject();

		sourcejson.put("s_featureKind", 2);
		sourcejson.put("s_Project", JSONObject.fromObject(null));
		sourcejson.put("s_sourceCode", 11);
		sourcejson.put("s_sourceId", JSONObject.fromObject(null));
		sourcejson.put("s_sourceType", "7");
		sourcejson.put("s_reliability", 100);

		put.addColumn("data".getBytes(), "source".getBytes(), sourcejson
				.toString().getBytes());

		JSONObject trackjson = new JSONObject();

		trackjson.put("t_lifecycle", 0);

		JSONArray trackinfoarray = new JSONArray();

		JSONObject trackinfo = new JSONObject();

		trackinfo.put("stage", 0);
		trackinfo.put("date", date);
		trackinfo.put("handler", 0);

		trackinfoarray.add(trackinfo);

		trackjson.put("t_trackInfo", trackinfoarray);

		put.addColumn("data".getBytes(), "track".getBytes(), trackjson
				.toString().getBytes());

		JSONObject dpattrjson = new JSONObject();

		JSONArray residarary = new JSONArray();

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

		injson.put("id", linkpid);
		injson.put("type", 1);
		dpattrjson.put("in", injson);

		JSONArray infoarary = new JSONArray();

		JSONArray oarary = new JSONArray();

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

		put.addColumn("data".getBytes(), "deep".getBytes(), dpattrjson
				.toString().getBytes());

		puts.add(put);
	}

	/**
	 * 删除oracle的表
	 * 
	 * @param conn
	 * @param tabName
	 * @throws SQLException
	 */
	public static void dropOracleTabIfExists(java.sql.Connection conn,
			String tabName) throws SQLException {

		Statement stmt = conn.createStatement();

		String sql = "select table_name from user_tables where table_name= upper('"
				+ tabName + "')";

		ResultSet resultSet = stmt.executeQuery(sql);

		if (resultSet.next()) {
			sql = "drop table " + tabName;

			stmt.execute(sql);
		}
	}

	/**
	 * 准备数据
	 * 
	 * @param conn
	 * @param progressManager
	 * @param scale
	 * @throws SQLException
	 */
	public static void prepareData(java.sql.Connection conn,
			ProgressService progressManager, double scale) throws SQLException {

		Statement stmt = conn.createStatement();

		dropOracleTabIfExists(conn, "restrict_tmp1");

		String sql_1 = "create table restrict_tmp1 as with tmp1 as (select c.pid,   a.link_pid,   b.node_pid,   c.restric_info in_info,   d.flag,   d.out_link_pid,   d.restric_info out_info,   format_vehicle(e.vehicle) vt,   d.detail_id    from rd_link                  a,         rd_node                  b,         rd_restriction           c,         rd_restriction_detail    d,         rd_restriction_condition e   where c.in_link_pid = a.link_pid     and c.node_pid = b.node_pid     and c.pid = d.restric_pid     and d.detail_id = e.detail_id(+)), tmp2 as (select a.pid, a.link_pid, a.node_pid, sum(vt) sum_vt    from tmp1 a   group by a.pid, a.link_pid, a.node_pid), resid as (select listagg(a.pid || '|' || case                   when a.sum_vt > 0 then                    1                   else                    0                 end,                 '-') within group(order by a.pid) resid,         a.link_pid,         a.node_pid      from tmp2 a   group by a.link_pid, a.node_pid), tmp4 as (select detail_id,         case           when sum_vt > 0 then            1           else            0         end sum_vt    from (select detail_id, sum(vt) sum_vt from tmp1 group by detail_id)), info as (select a.link_pid,         a.node_pid,         listagg(a.in_info || '|' || a.flag || '|' || b.sum_vt, '-') within group(order by a.detail_id) info    from tmp1 a, tmp4 b   where a.detail_id = b.detail_id   group by a.link_pid, a.node_pid)  select a.link_pid,a.node_pid, b.resid, a.info  from info a, resid b where a.link_pid = b.link_pid   and a.node_pid = b.node_pid ";

		stmt.execute(sql_1);

		progressManager.updateProgress("完成度:" + (int) (5 * scale) + "%");

		dropOracleTabIfExists(conn, "restrict_tmp2");

		sql_1 = "create table restrict_tmp2 as  select in_link_pid,        node_pid,        listagg(out_link_pid || '|' || restric_info || '|' || flag || '|' || vt || '|' ||                nvl(c_array, '^'),                '-') within group(order by out_link_pid) out_array   from (select out_link_pid,                a.restric_info,                flag,                decode(vt, null, 0, 0, 0, 1) vt,                c.in_link_pid,                c.node_pid,                d.c_array           from rd_restriction_detail a,                rd_restriction c,                (select detail_id, sum(format_vehicle(vehicle)) vt                   from rd_restriction_condition                  group by detail_id) b,                (select detail_id,                        listagg(nvl(time_domain,' ') || '@' ||                                decode(vt,                                       0,                                       null,                                       RES_TRAILER || '@' || RES_WEIGH || '@' ||                                       RES_AXLE_LOAD || '@' || RES_AXLE_COUNT || '@' ||                                       RES_OUT),                                '_') within group(order by detail_id) c_array                   from (select nvl(format_vehicle(vehicle), 0) vt, a.*                           from rd_restriction_condition a) b                  group by detail_id) d          where a.detail_id = b.detail_id(+)            and a.detail_id = d.detail_id(+)            and a.restric_pid = c.pid)  group by in_link_pid, node_pid";

		stmt.execute(sql_1);

		progressManager.updateProgress("完成度:" + (int) (10 * scale) + "%");

		// dropOracleTabIfExists(conn, "restrict_tmp3");

		sql_1 = "select a.link_pid, a.s_node_pid,a.e_node_pid,b.node_pid,a.geometry   "
				+ "from rd_link a, rd_node b, rd_restriction c   where c.in_link_pid = a.link_pid    "
				+ "and c.node_pid = b.node_pid ";

		ResultSet rs = stmt.executeQuery(sql_1);

		while (rs.next()) {

			int linkPid = rs.getInt(1);

			int sNodePid = rs.getInt(2);

			int eNodePid = rs.getInt(3);

			int nodePid = rs.getInt(4);

			STRUCT struct = (STRUCT) rs.getObject("geometry");

			JGeometry geom = JGeometry.load(struct);

			double[] point = calculateTipsPosition(sNodePid, eNodePid, nodePid,
					geom, 7);

			pointMap.put(linkPid + "," + nodePid, point);
		}

		progressManager.updateProgress("完成度:" + (int) (50 * scale) + "%");
	}

	/**
	 * 计算Tips的显示坐标
	 * 
	 * @param sNodePid
	 * @param eNodePid
	 * @param nodePid
	 * @param geom
	 * @param dis
	 * @return
	 */
	public static double[] calculateTipsPosition(int sNodePid, int eNodePid,
			int nodePid, JGeometry geom, int dis) {

		double startLon;

		double startLat;

		double stopLon;

		double stopLat;

		double startMerLon;

		double startMerLat;

		double stopMerLon;

		double stopMerLat;

		if (sNodePid == nodePid) {
			startLon = geom.getOrdinatesArray()[0];

			startLat = geom.getOrdinatesArray()[1];

			stopLon = geom.getOrdinatesArray()[2];

			stopLat = geom.getOrdinatesArray()[3];

		} else {
			int length = geom.getOrdinatesArray().length;

			startLon = geom.getOrdinatesArray()[length - 2];

			startLat = geom.getOrdinatesArray()[length - 1];

			stopLon = geom.getOrdinatesArray()[length - 4];

			stopLat = geom.getOrdinatesArray()[length - 3];
		}

		startMerLon = MercatorProjection.longitudeToMetersX(startLon);

		startMerLat = MercatorProjection.latitudeToMetersY(startLat);

		stopMerLon = MercatorProjection.longitudeToMetersX(stopLon);

		stopMerLat = MercatorProjection.latitudeToMetersY(stopLat);

		double point[] = calPoint(startMerLon, startMerLat, stopMerLon,
				stopMerLat, dis);

		point[0] = MercatorProjection.metersXToLongitude(point[0]);

		point[1] = MercatorProjection.metersYToLatitude(point[1]);

		return point;
	}

	/**
	 * 给定一条线段，计算在线上距离起点d米的点坐标
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param d
	 * @return
	 */
	public static double[] calPoint(double x1, double y1, double x2, double y2,
			int d) {

		double x0;

		double y0;

		if (x1 == x2) {

			x0 = x1;

			if (y1 < y2) {
				y0 = y1 + d;
			} else {
				y0 = y1 - d;
			}
		} else {

			double k = (y2 - y1) / (x2 - x1);

			if (x1 < x2) {

				x0 = d / Math.sqrt((Math.pow(k, 2) + 1)) + x1;
			} else {
				x0 = -d / Math.sqrt((Math.pow(k, 2) + 1)) + x1;
			}

			y0 = k * (x0 - x1) + y1;
		}

		return new double[] { x0, y0 };
	}

}
