package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

import net.sf.json.JSONObject;

public class RdSlopeSearch implements ISearch {

	private Connection conn;

	public RdSlopeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSlopeSelector selector = new RdSlopeSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);
		return obj;
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		RdSlopeSelector selector = new RdSlopeSelector(conn);

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT A.GEOMETRY, A.LINK_PID FROM RD_LINK A, RD_SLOPE B WHERE  sdo_within_distance (A.geometry,sdo_geometry ( :1, 8307),'DISTANCE=0') = 'TRUE' AND A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 ) SELECT A.PID, A.TYPE, A.LINK_PID, TMP1.GEOMETRY AS GEOMETRY FROM RD_SLOPE A, TMP1 WHERE A.LINK_PID = TMP1.LINK_PID AND A.U_RECORD != 2 ";

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

				m.put("a", resultSet.getInt("type"));

				snapshot.setM(m);

				snapshot.setT(24);

				snapshot.setI(resultSet.getInt("pid"));

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

	public static void main(String[] args) throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		RdSlopeSearch s = new RdSlopeSearch(conn);

		IObj obj = s.searchDataByPid(132837);

		System.out.println(obj.Serialize(null));
	}
}
