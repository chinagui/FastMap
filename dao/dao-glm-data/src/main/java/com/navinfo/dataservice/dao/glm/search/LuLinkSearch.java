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
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY,FORM FROM LU_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) index(c)*/ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(C.KIND, ',') WITHIN GROUP(ORDER BY B.NODE_PID) KINDS FROM TMP1 B LEFT JOIN LU_LINK A ON (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) AND A.U_RECORD !=2 LEFT JOIN LU_LINK_KIND C ON A.LINK_PID = C.LINK_PID AND C.U_RECORD !=2 GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) SAMNODEPART FROM TMP1 B LEFT JOIN RD_SAMENODE_PART A ON B.NODE_PID = A.NODE_PID AND A.TABLE_NAME = 'LU_NODE' GROUP BY B.NODE_PID, A.GROUP_ID), TMP4 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) AS INTERNODE FROM TMP1 B LEFT JOIN RD_INTER_NODE A ON B.NODE_PID = A.NODE_PID GROUP BY B.NODE_PID) SELECT A.NODE_PID, A.GEOMETRY, A.FORM, B.LINKPIDS, B.KINDS, C.SAMNODEPART, D.INTERNODE FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D WHERE A.NODE_PID = B.NODE_PID AND B.NODE_PID = C.NODE_PID AND D.NODE_PID = C.NODE_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				String linkPids = resultSet.getString("linkpids");

				String kinds = resultSet.getString("kinds");

				Map<String, JSONObject> linkMap = new HashMap<>();

				String linkPidArray[] = linkPids.split(",");

				for (int i = 0; i < linkPids.split(",").length; i++) {
					String linkPid = linkPidArray[i];

					JSONObject linkJSON = linkMap.get(linkPid);

					if (linkJSON != null) {
						String kind = linkJSON.getString("kinds");
						linkJSON.element("kinds", kind +","+kinds.split(",")[i]);
					} else {
						linkJSON = new JSONObject();

						linkJSON.put("linkPid", linkPid);

						linkJSON.put("kinds", kinds.split(",")[i]);
						
						String samNodePid = resultSet.getString("samNodePart");

						if (samNodePid != null) {
							linkJSON.put("sameNode", Integer.parseInt(samNodePid));
						} else {
							// 0代表没有同一点关系
							linkJSON.put("sameNode", 0);
						}

						String interNodePid = resultSet.getString("interNode");

						if (interNodePid != null) {
							linkJSON.put("interNode", Integer.parseInt(interNodePid));
						} else {
							// 0代表没有制作CRFI
							linkJSON.put("interNode", 0);
						}
						linkMap.put(linkPid, linkJSON);
					}
				}

				m.put("a", linkMap.values());
				
				//点的形态
				m.put("b", resultSet.getInt("form"));

				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(resultSet.getString("node_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				snapshot.setG(geojson.getJSONArray("coordinates"));

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

				m.put("a", resultSet.getString("s_node_pid"));

				m.put("b", resultSet.getString("e_node_pid"));

				int sameLinkKind = 1;

				if (resultSet.getInt("bua") > 0) {

					sameLinkKind = 2;

				} else if (resultSet.getInt("samekind") > 0) {

					sameLinkKind = 3;
				}

				m.put("c", sameLinkKind);

				snapshot.setM(m);

				snapshot.setT(29);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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