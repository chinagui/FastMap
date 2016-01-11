package com.navinfo.dataservice.FosEngine.edit.search;

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

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RdBranchSearch implements ISearch {
	
	private static final WKT wktSpatial = new WKT();
	
	private static final WKTReader wktReader = new WKTReader();

	private Connection conn;

	public RdBranchSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdBranchSelector selector = new RdBranchSelector(conn);

		IObj obj = (IObj) selector.loadHighwayById(pid, false);

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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE'), tmp2 as  (select a.in_link_pid,          a.node_pid,          listagg(a.branch_pid, ',') within group(order by 1) branch_pids     from rd_branch a, tmp1 b    where a.in_link_pid = b.link_pid      and exists (select null             from rd_branch_detail d            where a.branch_pid = d.branch_pid              and d.branch_type = 0)    group by a.in_link_pid, a.node_pid) select /*+ index(c) */  a.branch_pids, b.geometry link_geom, c.geometry point_geom   from tmp2 a, tmp1 b, rd_node c  where a.in_link_pid = b.link_pid    and a.node_pid = c.node_pid";

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
				
				snapshot.setT(7);

				jsonM.put("a",resultSet.getString("branch_pids").split(","));

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(wktSpatial.fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				String pointWkt = new String(wktSpatial.fromJGeometry(geom2));

				int direct = getDirect(linkWkt, pointWkt);
				
				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("c", String.valueOf((int)angle));

				double[][] point = DisplayUtils
						.getLinkPointPos(linkWkt, pointWkt, 1, 0);

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
	
	private static int getDirect(String linkWkt,String pointWkt) throws ParseException{
		
		int direct = 2;
		
		Geometry link = wktReader.read(linkWkt);
		
		Geometry point = wktReader.read(pointWkt);
		
		Coordinate[] csLink = link.getCoordinates();
		
		Coordinate cPoint = point.getCoordinate();
		
		if (csLink[0].x != cPoint.x || csLink[1].y != cPoint.y){
			direct = 3;
		}
		
		return direct;
	}

	public static void main(String[] args) throws Exception {
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		Connection conn = DBOraclePoolManager.getConnection(1);
		
		RdBranchSearch s = new RdBranchSearch(conn);
		
		IObj obj = s.searchDataByPid(3495);
		
		System.out.println(obj.Serialize(null));
	}
}
