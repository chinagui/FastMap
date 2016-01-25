package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;

public class RdSpeedlimitSearch implements ISearch {
	
	private Connection conn;

	public RdSpeedlimitSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);
		
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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2) select a.pid,        a.direct,        a.capture_flag || '|' || a.speed_flag || '|' || a.speed_value a_val,        b.geometry link_geom,        a.geometry point_geom   from rd_speedlimit a, tmp1 b  where a.link_pid = b.link_pid and a.u_record != 2";

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
				
				snapshot.setT(6);

				jsonM.put("a",resultSet.getString("a_val"));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);
				
				double angle = calAngle(resultSet);

				jsonM.put("c", String.valueOf((int)angle));

				snapshot.setG(Geojson.lonlat2Pixel(geom2.getFirstPoint()[0],geom2.getFirstPoint()[1],z,px,py));
				
				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	
	//计算角度
	private double calAngle(ResultSet resultSet)throws Exception {
		
		double angle = 0;
		
		STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom1 = JGeometry.load(struct1);
		
		double[] point = geom1.getFirstPoint();

		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		JGeometry geom2 = JGeometry.load(struct2);
		
		int ps = geom2.getNumPoints();
		
		int startIndex = 0;
		
		for(int i=0;i<ps-1;i++){
			double sx = geom2.getOrdinatesArray()[i * 2];
			
			double sy = geom2.getOrdinatesArray()[i * 2 + 1];
			
			double ex = geom2.getOrdinatesArray()[(i+1) * 2];
			
			double ey = geom2.getOrdinatesArray()[(i+1) * 2 + 1];
			
			if (isBetween(sx, ex, point[0]) && isBetween(sy, ey, point[1])){
				startIndex = i;
				break;
			}
		}
		
		
		StringBuilder sb = new StringBuilder("LINESTRING (");
		
		sb.append(geom2.getOrdinatesArray()[startIndex * 2]);
		
		sb.append(" ");
		
		sb.append(geom2.getOrdinatesArray()[startIndex * 2 + 1]);
		
		sb.append(", ");
		
		sb.append(geom2.getOrdinatesArray()[(startIndex +1) * 2]);
		
		sb.append(" ");
		
		sb.append(geom2.getOrdinatesArray()[(startIndex +1) * 2 + 1]);
		
		sb.append(")");
		
		angle = DisplayUtils.calIncloudedAngle(sb.toString(),
				resultSet.getInt("direct"));
		
		return angle;
		
		
	}
	
	private static boolean isBetween(double a, double b, double c) {
	    return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
	
	
	//通过传入点限速的LINKPID和通行方向，返回跟踪LINK路径
	public String trackSpeedLimitLink(int linkPid,int direct) throws Exception{
		
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);
		
		return selector.trackSpeedLimitLink(linkPid, direct);
	}
	
	
	public static void main(String[] args) throws Exception {
		ConfigLoader.initDBConn("C:/Users/lilei3774/Desktop/config.properties");
		
		Connection conn = DBOraclePoolManager.getConnection(11);
		
		RdSpeedlimitSearch a = new RdSpeedlimitSearch(conn);
		
		System.out.println(JSONArray.fromObject(a.searchDataByTileWithGap(0, 0, 0, 0)));
		
	}
}
