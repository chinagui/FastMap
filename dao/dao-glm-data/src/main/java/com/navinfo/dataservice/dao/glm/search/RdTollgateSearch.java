package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * 
 * @Title: RdTollgateSearch.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午3:43:35
 * @version: v1.0
 */
public class RdTollgateSearch implements ISearch {

	private Connection conn;

	public RdTollgateSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdTollgateSelector selector = new RdTollgateSelector(this.conn);
		IObj obj = (IObj) selector.loadByIdOrderBySeqnum(pid, true);
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

		String sql = "WITH TMP1 AS (SELECT A.GEOMETRY, A.NODE_PID FROM RD_NODE A, RD_TOLLGATE B WHERE SDO_RELATE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND A.NODE_PID = B.NODE_PID AND A.U_RECORD != 2) SELECT T.PID, T.TYPE, TMP1.GEOMETRY AS GEOMETRY FROM RD_TOLLGATE T, TMP1 WHERE T.NODE_PID = TMP1.NODE_PID AND T.U_RECORD != 2";		
		
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

				snapshot.setT(42);

				snapshot.setI(resultSet.getInt("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));
				
				JSONObject m = new JSONObject();
				
				m.put("a",resultSet.getInt("type"));
				
				snapshot.setM(m);

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
