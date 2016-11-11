package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdRoadSearch implements ISearch {

	private Connection conn;

	public RdRoadSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		return (IObj) new AbstractSelector(RdRoad.class, conn).loadById(pid,
				false);
	}
	
	@Override
	public IObj searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
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

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT /*+index(b)*/ B.PID, A.LINK_PID, A.GEOMETRY FROM TMP1 A, RD_ROAD_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2";

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

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setT(40);

				snapshot.setI(roadPid);

				Map<String, JSONObject> linkMap = values.get(roadPid);

				JSONArray gArray = new JSONArray();

				for (String linkpid : linkMap.keySet()) {

					JSONObject gObject = new JSONObject();

					gObject.put("i", linkpid);

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
