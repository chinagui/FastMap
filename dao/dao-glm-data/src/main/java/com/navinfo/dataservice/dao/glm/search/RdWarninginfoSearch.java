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
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdWarninginfoSearch implements ISearch{

	
	private Connection conn;

	public RdWarninginfoSearch(Connection conn) {
		this.conn = conn;
	}
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdWarninginfoSelector selector = new RdWarninginfoSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
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
		
		String sql = "WITH TMP1 AS (SELECT A.GEOMETRY, A.NODE_PID FROM RD_NODE A WHERE SDO_RELATE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND A.U_RECORD != 2) SELECT A.PID, A.TYPE_CODE, TMP1.GEOMETRY AS GEOMETRY FROM RD_WARNINGINFO A, TMP1 WHERE A.NODE_PID = TMP1.NODE_PID AND A.U_RECORD != 2";
		
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

				snapshot.setT(25);

				snapshot.setI(resultSet.getString("PID"));

				STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");
				
				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("TYPE_CODE"));

				snapshot.setM(m);

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
