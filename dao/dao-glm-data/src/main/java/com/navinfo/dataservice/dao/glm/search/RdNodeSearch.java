package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.ctc.wstx.util.StringUtil;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdNodeSearch implements ISearch {
	private static Logger logger = Logger.getLogger(RdNodeSearch.class);

	private Connection conn;

	private int dbId;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

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
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		RdNodeSelector selector = new RdNodeSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY, KIND FROM RD_NODE WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(A.IMI_CODE, ',') WITHIN GROUP(ORDER BY B.NODE_PID) IMICODES, LISTAGG(A.KIND, ',') WITHIN GROUP(ORDER BY B.NODE_PID) KINDS, LISTAGG(A.SPECIAL_TRAFFIC, ',') WITHIN GROUP(ORDER BY B.NODE_PID) TRAFFICS, LISTAGG(C.FORM_OF_WAY, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINK_FORMS FROM TMP1 B LEFT JOIN RD_LINK A ON A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID LEFT JOIN RD_LINK_FORM C ON A.LINK_PID = C.LINK_PID WHERE A.U_RECORD != 2 GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) SAMNODEPART FROM TMP1 B LEFT JOIN RD_SAMENODE_PART A ON B.NODE_PID = A.NODE_PID AND A.TABLE_NAME = 'RD_NODE' AND A.U_RECORD != 2 GROUP BY B.NODE_PID, A.GROUP_ID), TMP4 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) AS INTERNODE FROM TMP1 B LEFT JOIN RD_INTER_NODE A ON B.NODE_PID = A.NODE_PID AND A.U_RECORD != 2 GROUP BY B.NODE_PID), TMP5 AS (SELECT /*+ index(a) */ A.NODE_PID, LISTAGG(A.FORM_OF_WAY, ';') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_FORMS FROM RD_NODE_FORM A, TMP1 B WHERE A.U_RECORD != 2 AND A.NODE_PID = B.NODE_PID GROUP BY A.NODE_PID) SELECT A.NODE_PID, A.GEOMETRY, A.KIND, B.LINKPIDS, B.TRAFFICS, B.LINK_FORMS, B.IMICODES, B.KINDS, C.SAMNODEPART, D.INTERNODE, E.NODE_FORMS FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D, TMP5 E WHERE A.NODE_PID = B.NODE_PID AND B.NODE_PID = C.NODE_PID AND D.NODE_PID = C.NODE_PID AND A.NODE_PID = E.NODE_PID ";

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
						linkJSON.element("forms", form + ","
								+ forms.split(",")[i]);
					} else {
						linkJSON = new JSONObject();

						linkJSON.put("linkPid", linkPid);

						linkJSON.put("forms", forms.split(",")[i]);

						linkJSON.put("imiCode", resultSet.getString("IMICODES")
								.split(",")[i]);

						linkJSON.put("kinds", resultSet.getString("kinds")
								.split(",")[i]);

						linkJSON.put("specTraffic",
								resultSet.getString("TRAFFICS").split(",")[i]);

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

				m.put("form", resultSet.getString("node_forms"));

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
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, KIND, GEOMETRY FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS FROM RD_LINK A, TMP1 B WHERE A.U_RECORD != 2 AND (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ A.NODE_PID, LISTAGG(A.FORM_OF_WAY, ';') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_FORMS FROM RD_NODE_FORM A, TMP1 B WHERE A.U_RECORD != 2 AND A.NODE_PID = B.NODE_PID GROUP BY A.NODE_PID) SELECT A.NODE_PID, A.KIND, A.GEOMETRY, B.LINKPIDS, C.NODE_FORMS FROM TMP1 A, TMP2 B, TMP3 C WHERE A.NODE_PID = B.NODE_PID(+) AND A.NODE_PID = C.NODE_PID";

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

				int pid = resultSet.getInt("node_pid");

				/*
				 * RdNode node = (RdNode)this.searchDataByPid(pid); List<IRow>
				 * meshes = node.getMeshes(); for(IRow row:meshes){ RdNodeMesh }
				 */

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("linkpids"));

				m.put("b", resultSet.getString("node_forms"));

				m.put("c", resultSet.getInt("kind"));

				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(pid);

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator
						.geojson2Jts(geojson));
				List<Integer> linkMeshPids = new ArrayList<Integer>();
				if (dbIds.size() > 0) {
					for (int dbId : dbIds) {

						if (this.getDbId() == dbId) {
							continue;
						}

						Connection connection = null;
						try {
							logger.info("dbId========" + dbId);
							connection = DBConnector.getInstance()
									.getConnectionById(dbId);
							RdLinkSelector linkSelector = new RdLinkSelector(
									connection);
							List<Integer> linkPids = linkSelector
									.loadLinkPidByNodePid(pid, false);
							if (linkPids.size() > 0) {
								linkMeshPids.addAll(linkPids);
							}

						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							throw e;

						} finally {
							DBUtils.closeConnection(connection);
						}

					}
				}
				if (linkMeshPids.size() > 0 && m.containsKey("a")) {
					m.element("a", m.getString("a").concat("," + org.apache.commons.lang.StringUtils.join(linkMeshPids, ",")));
				}

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

	public List<SearchSnapshot> searchDataByNodePids(List<Integer> pids)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		if (null == pids || pids.size() == 0)
			return list;
		if (pids.size() > 1000) {
			return list;
		}

		String ids = org.apache.commons.lang.StringUtils.join(pids, ",");

		String sql = "WITH TMP1 AS (SELECT NODE_PID, KIND, GEOMETRY FROM RD_NODE WHERE NODE_PID IN ("
				+ ids
				+ ") AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS FROM RD_LINK A, TMP1 B WHERE A.U_RECORD != 2 AND (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ A.NODE_PID, LISTAGG(A.FORM_OF_WAY, ';') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_FORMS FROM RD_NODE_FORM A, TMP1 B WHERE A.U_RECORD != 2 AND A.NODE_PID = B.NODE_PID GROUP BY A.NODE_PID) SELECT A.NODE_PID, A.KIND, A.GEOMETRY, B.LINKPIDS, C.NODE_FORMS FROM TMP1 A, TMP2 B, TMP3 C WHERE A.NODE_PID = B.NODE_PID AND A.NODE_PID = C.NODE_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("linkpids"));

				m.put("b", resultSet.getString("node_forms"));

				m.put("c", resultSet.getInt("kind"));

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
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	public static void main(String[] args) {
		JSONObject m = new JSONObject();
		m.put("a", "111,222,333");
		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(444);
		linkPids.add(555);

		m.element(
				"a",
				m.getString("a").concat(
						","
								+ org.apache.commons.lang.StringUtils.join(
										linkPids, ",")));
		System.out.println(m);
	}
}
