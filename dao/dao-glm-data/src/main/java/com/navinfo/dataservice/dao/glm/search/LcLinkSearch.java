package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class LcLinkSearch implements ISearch {

	private Connection conn;

	public LcLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		LcLinkSelector lcLinkSelector = new LcLinkSelector(conn);

		IObj lcLink = (IObj) lcLinkSelector.loadById(pid, false);

		return lcLink;
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		LcLinkSelector selector = new LcLinkSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp as (select a.link_pid, max(b.kind) as kind from lc_link a, lc_link_kind b where sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE' and a.link_pid = b.link_pid and a.u_record != 2 and b.u_record != 2 group by a.link_pid) select t2.link_pid, t2.geometry, t2.s_node_pid, t2.e_node_pid, t1.kind from tmp t1, lc_link t2 where t1.link_pid = t2.link_pid";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("s_nodePid"));

				m.put("b", resultSet.getInt("e_nodePid"));

				m.put("c", resultSet.getInt("kind"));

				snapshot.setM(m);

				snapshot.setT(31);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return list;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP AS (SELECT LL.LINK_PID, WM_CONCAT(LLK.KIND) KIND FROM LC_LINK LL, LC_LINK_KIND LLK WHERE "
                + "SDO_WITHIN_DISTANCE(LL.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND LL.LINK_PID = LLK.LINK_PID "
                + "AND LL.U_RECORD <> 2 AND LLK.U_RECORD <> 2 GROUP BY LL.LINK_PID) SELECT T1.LINK_PID, T1.GEOMETRY, T1.S_NODE_PID, "
                + "T1.E_NODE_PID, T2.KIND FROM LC_LINK T1, TMP T2 WHERE T1.LINK_PID = T2.LINK_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("s_node_pid"));

				m.put("b", resultSet.getInt("e_node_pid"));

				m.put("c", resultSet.getString("kind"));

				snapshot.setM(m);

				snapshot.setT(31);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JGeometry geo = JGeometry.load(struct);

				if (geo.getType() != 2) {
					continue;
				}

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return list;
	}
	
	
	public List<SearchSnapshot> searchDataByLinkPids(List<Integer> pids)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		if (null == pids || pids.size() == 0||pids.size() > 1000)
		{
			return list;
		}
		
		String ids = org.apache.commons.lang.StringUtils.join(pids, ",");

		String sql = "WITH TMP AS (SELECT A.LINK_PID, MAX(B.KIND) AS KIND FROM LC_LINK A, LC_LINK_KIND B WHERE  A.LINK_PID IN ("
				+ ids
				+ ") AND A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 AND B.U_RECORD != 2 GROUP BY A.LINK_PID) SELECT T2.LINK_PID, T2.GEOMETRY, T2.S_NODE_PID, T2.E_NODE_PID, T1.KIND FROM TMP T1, LC_LINK T2 WHERE T1.LINK_PID = t2.link_pid";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("s_node_pid"));

				m.put("b", resultSet.getInt("e_node_pid"));

				m.put("c", resultSet.getInt("kind"));

				snapshot.setM(m);

				snapshot.setT(31);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return list;
	}

	public static void main(String[] args) throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(11);

		LcLinkSearch search = new LcLinkSearch(conn);

		List<SearchSnapshot> res = search.searchDataByTileWithGap(215829,
				99329, 18, 20);

		for (SearchSnapshot s : res) {
			System.out.println(s.Serialize(null));
		}
	}
}