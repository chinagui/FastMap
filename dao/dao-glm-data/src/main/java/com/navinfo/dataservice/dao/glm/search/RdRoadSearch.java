package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;

import org.apache.log4j.Logger;

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

import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class RdRoadSearch implements ISearch {
	private Logger logger = Logger.getLogger(RdRoadSearch.class);

	private Connection conn;

	private int dbId;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public RdRoadSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		return (IObj) new AbstractSelector(RdRoad.class, conn).loadById(pid,
				false);
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		AbstractSelector selector = new AbstractSelector(RdRoad.class, conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		return null;
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

		String sql = "WITH TMP1 AS (SELECT LINK_PID FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ distinct（A.PID） ROAD_PID FROM RD_ROAD_LINK A, TMP1 B WHERE B.LINK_PID = A.LINK_PID AND A.U_RECORD != 2) SELECT /*+index(b)*/ B.PID, C.LINK_PID, C.GEOMETRY FROM TMP2 A, RD_ROAD_LINK B, RD_LINK C WHERE A.ROAD_PID = B.PID AND C.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 AND C.U_RECORD !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			// Map<String:crf道路pid, Map<String：linkpid, JSONArray：link几何json组>>
			Map<String, Map<String, JSONObject>> values = new HashMap<String, Map<String, JSONObject>>();

			while (resultSet.next()) {

				String roadPid = resultSet.getString("PID");

				if (!values.containsKey(roadPid)) {

					values.put(roadPid, new HashMap<String, JSONObject>());
				}

				Map<String, JSONObject> linkMap = values.get(roadPid);

				String linkPid = resultSet.getString("LINK_PID");

				STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.coord2Pixel(geojson, z, px, py);

				linkMap.put(linkPid, geojson);
			}

			for (String roadPid : values.keySet()) {
				int pid = Integer.parseInt(roadPid);

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setT(40);

				snapshot.setI(pid);
				RdRoad rdRoad = (RdRoad) this.searchDataByPid(pid);
				Map<String, JSONObject> linkMap = values.get(pid);

				if (this.isCheckLinkArea(rdRoad, linkMap)) {
					this.addRdRoadForArea(rdRoad, linkMap, wkt);

				}
				JSONArray gArray = new JSONArray();

				for (String linkpid : linkMap.keySet()) {

					JSONObject gObject = new JSONObject();

					gObject.put("i", Integer.parseInt(linkpid));

					gObject.put("g",
							linkMap.get(linkpid).getJSONArray("coordinates"));

					gArray.add(gObject);
				}

				snapshot.setG(gArray);

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

	private boolean isCheckLinkArea(RdRoad rdRoad,
			Map<String, JSONObject> linkMap) {

		if (rdRoad.getLinks() != null && rdRoad.getLinks().size() > 0) {
			if (rdRoad.getLinks().size() != linkMap.size()) {
				return true;
			}
		}
		return false;
	}

	private void addRdRoadForArea(RdRoad rdRoad,
			Map<String, JSONObject> linkMap, String wkt) throws Exception {
		Set<Integer> dbIds = null;
		String[] meshIds = MeshUtils.geometry2Mesh(GeoTranslator
				.wkt2Geometry(wkt));
		Set<String> extendMeshes = null;
		if (meshIds != null && meshIds.length > 0) {
			extendMeshes = MeshUtils.getNeighborMeshSet(
					new HashSet<>(Arrays.asList(meshIds)), 3);
		}
		if (extendMeshes != null) {
			dbIds = DbMeshInfoUtil.calcDbIds(extendMeshes);
		}
		List<Integer> links = new ArrayList<Integer>();

		for (IRow row : rdRoad.getLinks()) {
			RdRoadLink roadLink = (RdRoadLink) row;
			if (!linkMap.containsKey(String.valueOf(roadLink.getLinkPid()))) {
				links.add(roadLink.getLinkPid());

			}
		}
		for (int dbId : dbIds) {

			Connection connection = null;
			try {
				logger.info("dbId========" + dbId);
				connection = DBConnector.getInstance().getConnectionById(dbId);
				if (this.getDbId() == dbId) {
					continue;
				}
				if (links.size() > 0) {
					RdLinkSelector linkSelector = new RdLinkSelector(connection);
					List<IRow> rows = linkSelector.loadByIds(links, false,
							false);
					if (rows.size() > 0) {
						for (IRow row : rows) {
							RdLink link = (RdLink) row;
							linkMap.put(
									String.valueOf(link.getPid()),
									GeoTranslator.jts2Geojson(
											link.getGeometry(), 0.00001, 5));

						}
					}
				}
				if (!this.isCheckLinkArea(rdRoad, linkMap)) {
					break;
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e;

			} finally {
				DBUtils.closeConnection(connection);
			}

		}

	}

	public List<SearchSnapshot> searchDataByRoadPids(List<Integer> pids)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<>();

		if (null == pids || pids.size() == 0) {

			return list;
		}
		if (pids.size() > 1000) {

			return list;
		}

		String ids = org.apache.commons.lang.StringUtils.join(pids, ",");

		String sql = "SELECT A.PID, A.LINK_PID, B.GEOMETRY FROM RD_ROAD_LINK A LEFT JOIN RD_LINK B ON A.LINK_PID = B.LINK_PID WHERE A.U_RECORD <> 2 AND B.U_RECORD <> 2 AND A.PID IN ( "
				+ ids + " )";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			// Map<String:crf道路pid, Map<String：linkpid, JSONArray：link几何json组>>
			Map<String, Map<String, JSONObject>> values = new HashMap<String, Map<String, JSONObject>>();

			while (resultSet.next()) {

				String roadPid = resultSet.getString("PID");

				if (!values.containsKey(roadPid)) {

					values.put(roadPid, new HashMap<String, JSONObject>());
				}

				Map<String, JSONObject> linkMap = values.get(roadPid);

				String linkPid = resultSet.getString("LINK_PID");

				STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				linkMap.put(linkPid, geojson);
			}

			for (String roadPid : values.keySet()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setT(40);

				snapshot.setI(Integer.parseInt(roadPid));

				Map<String, JSONObject> linkMap = values.get(roadPid);

				JSONArray gArray = new JSONArray();

				for (String linkpid : linkMap.keySet()) {

					JSONObject gObject = new JSONObject();

					gObject.put("i", Integer.parseInt(linkpid));

					gObject.put("g",
							linkMap.get(linkpid).getJSONArray("coordinates"));

					gArray.add(gObject);
				}

				snapshot.setG(gArray);

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
}
