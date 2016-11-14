package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class ZoneLinkSearch implements ISearch {

	private Connection conn;

	public ZoneLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		ZoneLinkSelector selector = new ZoneLinkSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.link_pid,        a.geometry,        a.s_node_pid,        a.e_node_pid   from zone_link a,          where a.u_record != 2      and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("s_nodePid"));

				m.put("b", resultSet.getString("e_nodePid"));

				snapshot.setM(m);

				snapshot.setT(18);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

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

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, S_NODE_PID, E_NODE_PID FROM ZONE_LINK WHERE U_RECORD != 2 AND SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE') SELECT A.*, (SELECT COUNT(1) FROM ZONE_LINK_KIND Z WHERE Z.LINK_PID = A.LINK_PID AND Z.U_RECORD != 2 AND Z.KIND = 1) AOI, (SELECT COUNT(1) FROM ZONE_LINK_KIND Z WHERE Z.LINK_PID = A.LINK_PID AND Z.U_RECORD != 2 AND Z.KIND = 2) KD FROM TMP1 A";
		
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

				m.put("a", resultSet.getString("s_node_pid"));

				m.put("b", resultSet.getString("e_node_pid"));

				int sameLinkKind = 0;

				if (resultSet.getInt("kd") >0) {

					sameLinkKind = 2;

				} else if (resultSet.getInt("aoi") > 0) {

					sameLinkKind = 1;
				}

				m.put("c", sameLinkKind);

				snapshot.setM(m);

				snapshot.setT(18);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

		}

		return list;
	}

}