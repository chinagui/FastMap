package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class RdDirectrouteSearch implements ISearch {

	private Connection conn;

	public RdDirectrouteSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID,S_NODE_PID,E_NODE_PID, GEOMETRY FROM RD_LINK WHERE sdo_within_distance (geometry,sdo_geometry ( :1, 8307),'DISTANCE=0') = 'TRUE' AND U_RECORD != 2) SELECT  A.PID, A.node_pid,C.s_node_pid,C.e_node_pid,C.GEOMETRY  FROM RD_DIRECTROUTE A, TMP1 C WHERE A.IN_LINK_PID = C.LINK_PID AND A.U_RECORD != 2 ";

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
				JSONObject jsonM = new JSONObject();

				snapshot.setT(35);

				snapshot.setI(resultSet.getInt("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				JGeometry geom = JGeometry.load(struct);

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				int sNodePid = resultSet.getInt("s_node_pid");

				int eNodePid = resultSet.getInt("e_node_pid");

				int nodePid = resultSet.getInt("node_pid");
				double angle = AngleCalculator.getDisplayAngle(nodePid,
						sNodePid, eNodePid, geom);

				jsonM.put("a", String.valueOf((int) angle));

				snapshot.setG(jo.getJSONArray("coordinates"));
				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new SQLException(e);
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

}
