package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdLinkSpeedLimitSearch implements ISearch {

	private Connection conn;
	
	String queryType="";

	public RdLinkSpeedLimitSearch(Connection conn) {
		this.conn = conn;
	}

	public RdLinkSpeedLimitSearch(Connection conn, String queryType) {
		this.conn = conn;
		this.queryType = queryType;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		// TODO Auto-generated method stub
		return null;
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

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT /*+ index(a) */ A.LINK_PID, A.FROM_SPEED_LIMIT, A.FROM_LIMIT_SRC, A.TO_SPEED_LIMIT, A.TO_LIMIT_SRC, A.SPEED_DEPENDENT, A.SPEED_TYPE, B.GEOMETRY LINK_GEOM FROM RD_LINK_SPEEDLIMIT A, TMP1 B WHERE A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 ";
			
		if (queryType.equals("DEPENDENT")) {

			sql += " AND A.SPEED_TYPE = 3 ";

		} else {

			sql += " AND A.SPEED_TYPE IN (0,1)";
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);
			
			Map<Integer,SearchSnapshot> snapshotMap=new HashMap<Integer,SearchSnapshot>();
			
			while (resultSet.next()) {

				int fromSpeedLimit = resultSet.getInt("from_speed_limit");

				int toSpeedLimit = resultSet.getInt("to_speed_limit");

				if (fromSpeedLimit == 0 && toSpeedLimit == 0) {

					continue;
				}
				
				int fromLimitSrc = resultSet.getInt("from_limit_src");

				int toLimitSrc = resultSet.getInt("to_limit_src");

				int link_pid = resultSet.getInt("link_pid");
				
				SearchSnapshot snapshot =null;

				JSONObject jsonM = null;

				JSONArray meArray = null;

				if (snapshotMap.containsKey(link_pid)) {
					snapshot = snapshotMap.get(link_pid);
					jsonM = snapshot.getM();
					meArray = jsonM.getJSONArray("e");
				} else {
					snapshot = new SearchSnapshot();
					snapshotMap.put(link_pid, snapshot);
					snapshot.setI(link_pid);
					snapshot.setT(9);
					jsonM = new JSONObject();
					meArray = new JSONArray();
					
					jsonM.put("f", resultSet.getInt("speed_dependent"));

					jsonM.put("g", resultSet.getInt("speed_type"));
				}

				int direct = 0;

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry linkGeom = JGeometry.load(struct1);

				WKT wktSpatial = new WKT();

				String linkWkt = new String(wktSpatial.fromJGeometry(linkGeom));
				
				String info = String.valueOf(fromSpeedLimit) + ","
						+ String.valueOf(fromLimitSrc) + ","
						+ String.valueOf(toSpeedLimit) + ","
						+ String.valueOf(toLimitSrc);

				meArray.add(info);
				
				jsonM.put("e", meArray);
				
				if (fromSpeedLimit > 0) {

					direct = 2;
					
					if (!jsonM.containsKey("a") && !jsonM.containsKey("b")) {

						double[] position = DisplayUtils.getMid2MPosition(
								linkWkt, direct, DisplayUtils.getVerUint(z));

						jsonM.put("a", Geojson.lonlat2Pixel(position[0],
								position[1], z, px, py));

						double angle = calAngle(position, linkGeom, direct);
						
						jsonM.put("b", angle);
					}
				}

				if (toSpeedLimit > 0) {

					direct = 3;

					if (!jsonM.containsKey("c") && !jsonM.containsKey("d")) {

						double[] position = DisplayUtils.getMid2MPosition(
								linkWkt, direct, DisplayUtils.getVerUint(z));

						jsonM.put("c", Geojson.lonlat2Pixel(position[0],
								position[1], z, px, py));

						double angle = calAngle(position, linkGeom, direct);

						jsonM.put("d", angle);
					}
				}

				
				snapshot.setM(jsonM);
			}
			
			list.addAll(snapshotMap.values());
		} catch (Exception e) {

			throw new SQLException(e);
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return list;
	}
	
	
	

	// 计算角度

	/**
	 * 计算角度
	 * 
	 * @param point
	 * @param resultSet
	 * @param direct
	 * @return
	 * @throws Exception
	 */
	private double calAngle(double[] point, JGeometry linkGeom, int direct)
			throws Exception {

		double angle = 0;

		// STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");
		//
		// JGeometry geom1 = JGeometry.load(struct1);

		// double[] point = geom1.getFirstPoint();

		// STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");
		//
		// JGeometry geom2 = JGeometry.load(struct2);

		int ps = linkGeom.getNumPoints();

		int startIndex = 0;

		for (int i = 0; i < ps - 1; i++) {
			double sx = linkGeom.getOrdinatesArray()[i * 2];

			double sy = linkGeom.getOrdinatesArray()[i * 2 + 1];

			double ex = linkGeom.getOrdinatesArray()[(i + 1) * 2];

			double ey = linkGeom.getOrdinatesArray()[(i + 1) * 2 + 1];

			if (isBetween(sx, ex, point[2]) && isBetween(sy, ey, point[3])) {
				startIndex = i;
				break;
			}
		}

		StringBuilder sb = new StringBuilder("LINESTRING (");

		sb.append(linkGeom.getOrdinatesArray()[startIndex * 2]);

		sb.append(" ");

		sb.append(linkGeom.getOrdinatesArray()[startIndex * 2 + 1]);

		sb.append(", ");

		sb.append(linkGeom.getOrdinatesArray()[(startIndex + 1) * 2]);

		sb.append(" ");

		sb.append(linkGeom.getOrdinatesArray()[(startIndex + 1) * 2 + 1]);

		sb.append(")");

		angle = DisplayUtils.calIncloudedAngle(sb.toString(), direct);

		return angle;

	}

	private static boolean isBetween(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}
}
