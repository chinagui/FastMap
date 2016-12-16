package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

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

		String sql = "with tmp1 as  (select link_pid, geometry  from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2) select /*+ index(a) */  a.link_pid,  a.from_speed_limit,  a.from_limit_src,  a.to_speed_limit,  a.to_limit_src,  b.geometry link_geom   from rd_link_speedlimit a, tmp1 b  where a.link_pid = b.link_pid    and a.speed_type = 0    and a.u_record != 2 ";

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

				int fromSpeedLimit = resultSet.getInt("from_speed_limit");

				int toSpeedLimit = resultSet.getInt("to_speed_limit");

				if (fromSpeedLimit == 0 && toSpeedLimit == 0) {

					continue;
				}

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject jsonM = new JSONObject();

				snapshot.setI(resultSet.getInt("link_pid"));

				snapshot.setT(9);

				int direct = 0;

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry linkGeom = JGeometry.load(struct1);

				WKT wktSpatial = new WKT();

				String linkWkt = new String(wktSpatial.fromJGeometry(linkGeom));

				if (fromSpeedLimit > 0) {

					direct = 2;
					
					double[] position = DisplayUtils.getMid2MPosition(linkWkt,
							direct,DisplayUtils.getVerUint(z));

					jsonM.put("a", Geojson.lonlat2Pixel(position[0],
							position[1], z, px, py));

					double angle = calAngle(position, linkGeom, direct);

					int fromLimitSrc = resultSet.getInt("from_limit_src");

					String info = String.valueOf(fromSpeedLimit) + ","
							+ String.valueOf(fromLimitSrc) + ","
							+ String.valueOf(angle);

					jsonM.put("b", info);
				}

				if (toSpeedLimit > 0) {

					direct = 3;

					double[] position = DisplayUtils.getMid2MPosition(linkWkt,
							direct,DisplayUtils.getVerUint(z));

					jsonM.put("c", Geojson.lonlat2Pixel(position[0],
							position[1], z, px, py));

					double angle = calAngle(position, linkGeom, direct);

					int toLimitSrc = resultSet.getInt("to_limit_src");

					String info = String.valueOf(toSpeedLimit) + ","
							+ String.valueOf(toLimitSrc) + ","
							+ String.valueOf(angle);

					jsonM.put("d", info);
				}

				snapshot.setM(jsonM);

				list.add(snapshot);
			}
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
