package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdElectroniceyeSearch implements ISearch {

	private Connection conn;

	public RdElectroniceyeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
		IObj obj = (IObj) selector.loadById(pid, false);
		return obj;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();
		String sql = "select a.pid, a.geometry from rd_electroniceye a where a.u_record <> 2 and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE'";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setT(12);

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != resultSet)
					resultSet.close();
				if (null != pstmt)
					pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
		return this.searchDataBySpatial(wkt);
	}
	
	public static void main(String[] args) {
		String wkt = MercatorProjection.getWktWithGap(215885, 99231, 18, 80);
		System.out.println(256 << 18);
		
	}

}
