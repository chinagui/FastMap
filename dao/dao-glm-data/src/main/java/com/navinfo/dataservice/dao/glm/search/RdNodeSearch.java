package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdNodeSearch implements ISearch {

	private Connection conn;

	public RdNodeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdNodeSelector selector = new RdNodeSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
	}

	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY,kind FROM RD_NODE WHERE sdo_within_distance(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(A.IMI_CODE, ',') WITHIN GROUP(ORDER BY B.NODE_PID) IMICODES, LISTAGG(A.SPECIAL_TRAFFIC, ',') WITHIN GROUP(ORDER BY B.NODE_PID) TRAFFICS, LISTAGG(C.FORM_OF_WAY, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINK_FORMS FROM TMP1 B LEFT JOIN RD_LINK A ON A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID LEFT JOIN RD_LINK_FORM C ON A.LINK_PID = C.LINK_PID WHERE A.U_RECORD != 2 GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) SAMNODEPART FROM TMP1 B LEFT JOIN RD_SAMENODE_PART A ON B.NODE_PID = A.NODE_PID AND A.TABLE_NAME = 'RD_NODE' AND A.U_RECORD !=2 GROUP BY B.NODE_PID, A.GROUP_ID), TMP4 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) AS INTERNODE FROM TMP1 B LEFT JOIN RD_INTER_NODE A ON B.NODE_PID = A.NODE_PID AND A.U_RECORD !=2 GROUP BY B.NODE_PID) SELECT A.NODE_PID, A.GEOMETRY, A.KIND,B.LINKPIDS, B.TRAFFICS, B.LINK_FORMS, B.IMICODES, C.SAMNODEPART, D.INTERNODE FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D WHERE A.NODE_PID = B.NODE_PID AND B.NODE_PID = C.NODE_PID AND D.NODE_PID = C.NODE_PID  ";

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

				String forms = resultSet.getString("link_forms");

				Map<String, JSONObject> linkMap = new HashMap<>();

				String linkPidArray[] = linkPids.split(",");

				for (int i = 0; i < linkPids.split(",").length; i++) {
					String linkPid = linkPidArray[i];

					JSONObject linkJSON = linkMap.get(linkPid);

					if (linkJSON != null) {
						String form = linkJSON.getString("forms");
						linkJSON.element("forms", form +","+forms.split(",")[i]);
					} else {
						linkJSON = new JSONObject();

						linkJSON.put("linkPid", linkPid);

						linkJSON.put("forms", forms.split(",")[i]);

						linkJSON.put("imiCode", resultSet.getString("IMICODES").split(",")[i]);
						
						linkJSON.put("specTraffic", resultSet.getString("TRAFFICS").split(",")[i]);

						linkMap.put(linkPid, linkJSON);
					}
				}
				
				m.put("a", linkMap.values());
				
				String samNodePid = resultSet.getString("samNodePart");

				if (samNodePid != null) {
					m.put("sameNode", samNodePid);
				} else {
					// 0代表没有同一点关系
					m.put("sameNode", 0);
				}

				String interNodePid = resultSet.getString("interNode");

				if (interNodePid != null) {
					m.put("interNode", interNodePid);
				} else {
					// 0代表没有制作CRFI
					m.put("interNode", 0);
				}
				
				m.put("kind", resultSet.getInt("kind"));
				
				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(resultSet.getInt("node_pid"));

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
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS FROM RD_LINK A, TMP1 B WHERE A.U_RECORD != 2 AND (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ A.NODE_PID, LISTAGG(A.FORM_OF_WAY, ';') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_FORMS FROM RD_NODE_FORM A, TMP1 B WHERE A.U_RECORD != 2 AND A.NODE_PID = B.NODE_PID GROUP BY A.NODE_PID) SELECT A.NODE_PID, A.GEOMETRY, B.LINKPIDS, C.NODE_FORMS FROM TMP1 A, TMP2 B, TMP3 C WHERE A.NODE_PID = B.NODE_PID AND A.NODE_PID = C.NODE_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("linkpids"));
				
				m.put("b", resultSet.getString("node_forms"));

				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(resultSet.getInt("node_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

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
