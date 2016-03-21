package com.navinfo.dataservice.engine.edit.edit.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.engine.edit.edit.model.IObj;

public class RdLinkSpeedLimitSearch implements ISearch {

	private Connection conn;

	public RdLinkSpeedLimitSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		// TODO Auto-generated method stub
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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2) select /*+ index(a) */  a.link_pid,  a.from_speed_limit,  a.from_limit_src,  a.to_speed_limit,  a.to_limit_src,  b.geometry link_geom   from rd_link_speedlimit a, tmp1 b  where a.link_pid = b.link_pid    and a.speed_type = 0    and a.u_record != 2 ";

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

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));
				
				snapshot.setT(9);

				jsonM.put("a",resultSet.getString("from_speed_limit")+","+resultSet.getString("to_speed_limit"));

				jsonM.put("b",resultSet.getString("from_speed_src")+","+resultSet.getString("to_speed_src"));
				
				String linkWkt = resultSet.getString("link_geom");
				
				int direct = 2;
				
				if (resultSet.getInt("from_speed_limit") == 0){
					direct = 3;
				}
				
				double [] position = DisplayUtils.getMid2MPosition(linkWkt, direct);

				snapshot.setG(Geojson.lonlat2Pixel(position[0],position[1],z,px,py));
				
				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {
			
			throw new SQLException(e);
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
