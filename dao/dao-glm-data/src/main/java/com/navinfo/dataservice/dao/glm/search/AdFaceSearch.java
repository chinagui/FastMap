package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;

public class AdFaceSearch implements ISearch {

	private Connection conn;

	public AdFaceSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return null;
	}


	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.face_pid,        a.geometry,   from ad_face a,          where a.u_record != 2      and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

                snapshot.setT(13);

                snapshot.setI(String.valueOf(resultSet.getInt("face_pid")));

                STRUCT struct = (STRUCT) resultSet.getObject("geometry");

                JSONObject jo = Geojson.spatial2Geojson(struct);

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

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.face_pid,        a.geometry,   from ad_face a,          where a.u_record != 2      and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			
			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();
			
			double px =  MercatorProjection.tileXToPixelX(x);
			
			double py =  MercatorProjection.tileYToPixelY(y);
			
			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

                snapshot.setT(13);
                
                snapshot.setI(String.valueOf(resultSet.getInt("face_pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.face2Pixel(geojson, px, py, z);
				
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

}
