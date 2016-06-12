package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.vividsolutions.jts.io.WKTReader;

public class RdLaneConnexitySearch implements ISearch {
	private WKT wktSpatial = new WKT();
	
	private WKTReader wktReader = new WKTReader();

	private Connection conn;

	public RdLaneConnexitySearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2) select /*+ index(c) */  a.pid, a.lane_info, b.geometry link_geom, c.geometry point_geom   from rd_lane_connexity a, tmp1 b, rd_node c  where a.in_link_pid = b.link_pid    and a.node_pid = c.node_pid    and a.u_record != 2 and c.u_record != 2";

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

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));
				
				snapshot.setT(5);

				jsonM.put("b",resultSet.getString("lane_info"));

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(wktSpatial.fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				String pointWkt = new String(wktSpatial.fromJGeometry(geom2));

				int direct = DisplayUtils.getDirect(linkWkt, pointWkt);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("c", String.valueOf((int)angle));

				double[][] point = DisplayUtils
						.getGdbPointPos(linkWkt, pointWkt, 1);

				snapshot.setG(Geojson.lonlat2Pixel(point[1][0],point[1][1],z,px,py));
				
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
	

	public static void main(String[] args) throws Exception {
		
		Connection conn = DBConnector.getInstance().getConnectionById(11);
		
		RdLaneConnexitySearch s = new RdLaneConnexitySearch(conn);
		
		List<SearchSnapshot> ss = s.searchDataByTileWithGap(107939,49614,17, 1);
		
		for(SearchSnapshot n : ss){
			System.out.println(n.Serialize(null));
		}
	}

}
