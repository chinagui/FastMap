package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class LuLinkSearch implements ISearch {

	private Connection conn;

	public LuLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		LuLinkSelector luLinkSelector = new LuLinkSelector(conn);

		IObj luLink = (IObj) luLinkSelector.loadById(pid, false);

		return luLink;
	}
	
	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {
		
		LuLinkSelector selector = new LuLinkSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = " WITH TMP1 AS (SELECT A.LINK_PID, A.GEOMETRY, A.S_NODE_PID, A.E_NODE_PID FROM LU_LINK A WHERE SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND A.U_RECORD != 2), TMP2 AS /*+ index(P) */ (SELECT P.LINK_PID, S.GROUP_ID SAMELINK_PID FROM RD_SAMELINK_PART P, RD_SAMELINK S, TMP1 L WHERE P.LINK_PID = L.LINK_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :2 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP3 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID S_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.S_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :3 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP4 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID E_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.E_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :4 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP5 AS (SELECT /*+ index(a) */ A.LINK_PID, LISTAGG(A.KIND, ';') WITHIN GROUP(ORDER BY A.LINK_PID) KINDS FROM LU_LINK_KIND A, TMP1 B WHERE A.U_RECORD != 2 AND A.LINK_PID = B.LINK_PID GROUP BY A.LINK_PID) SELECT A.*, B.SAMELINK_PID, C.S_SAMENODEPID, D.E_SAMENODEPID, E.KINDS FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D, TMP5 E WHERE A.LINK_PID = B.LINK_PID(+) AND A.LINK_PID = C.LINK_PID(+) AND A.LINK_PID = D.LINK_PID(+) AND A.LINK_PID = E.LINK_PID(+) ";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);
			pstmt.setString(2, "LU_LINK");
			pstmt.setString(3, "LU_NODE");
			pstmt.setString(4, "LU_NODE");

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("s_node_pid"));

				m.put("b", resultSet.getInt("e_node_pid"));
				
				m.put("c", resultSet.getString("kinds"));
				
				m.put("d", resultSet.getInt("samelink_pid"));
				
				m.put("e", resultSet.getInt("s_samenodepid"));
				
				m.put("f", resultSet.getInt("e_samenodepid"));

				snapshot.setM(m);

				snapshot.setT(18);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeStatement(pstmt);
			DBUtils.closeResultSet(resultSet);
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

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY, S_NODE_PID, E_NODE_PID FROM LU_LINK WHERE U_RECORD != 2 AND SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE') SELECT A.*, (SELECT COUNT(1) FROM LU_LINK_KIND L WHERE L.LINK_PID = A.LINK_PID AND L.U_RECORD != 2 AND L.KIND = 21) BUA, (SELECT COUNT(1) FROM LU_LINK_KIND L WHERE L.LINK_PID = A.LINK_PID AND L.U_RECORD != 2 AND L.KIND IN (1,2,3,4,5,6,7,22,23,40)) SAMEKIND FROM TMP1 A";
		
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

				int sameLinkKind = 1;

				if (resultSet.getInt("bua") > 0) {

					sameLinkKind = 2;

				} else if (resultSet.getInt("samekind") > 0) {

					sameLinkKind = 3;
				}

				m.put("c", sameLinkKind);

				snapshot.setM(m);

				snapshot.setT(29);

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

	public static void main(String[] args) throws Exception {
		Connection conn = DBConnector.getInstance().getConnectionById(11);

		LuLinkSearch search = new LuLinkSearch(conn);

		List<SearchSnapshot> res = search.searchDataByTileWithGap(215829,
				99329, 18, 20);

		for (SearchSnapshot s : res) {
			System.out.println(s.Serialize(null));
		}
	}
}