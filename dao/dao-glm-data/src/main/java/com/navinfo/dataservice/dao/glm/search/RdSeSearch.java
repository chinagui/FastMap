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
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;

import net.sf.json.JSONObject;

/**
 * @Title: RdSeSearch.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午3:45:22
 * @version: v1.0
 */
public class RdSeSearch implements ISearch {

	private Connection conn;

	public RdSeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSeSelector rdSeSelector = new RdSeSelector(this.conn);
		IObj obj = (IObj) rdSeSelector.loadById(pid, true);
		return obj;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(A.GEOMETRY) as geometry, A.NODE_PID FROM RD_NODE A, RD_SE B WHERE SDO_RELATE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND A.NODE_PID = B.NODE_PID AND A.U_RECORD != 2 group by SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(A.GEOMETRY),A.NODE_PID ) SELECT A.PID, A.IN_LINK_PID, A.OUT_LINK_PID, A.NODE_PID, TMP1.GEOMETRY AS GEOMETRY FROM RD_SE A, TMP1 WHERE A.NODE_PID = TMP1.NODE_PID AND A.U_RECORD != 2 ";

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

				m.put("a", resultSet.getInt("in_link_pid"));

				m.put("b", resultSet.getString("out_link_pid"));

				snapshot.setM(m);

				snapshot.setT(34);

				snapshot.setI(resultSet.getInt("pid"));

				String pointWkt = resultSet.getString("geometry");

				JSONObject geojson = Geojson.wkt2Geojson(pointWkt);

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
