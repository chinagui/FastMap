package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
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
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		// String sql = "select pid, direct, angle, geometry from
		// rd_electroniceye where sdo_relate(geometry, sdo_geometry(:1,
		// 8307),'mask=anyinteract') = 'TRUE' and u_record != 2";
		String sql = "SELECT a.pid, a.direct, a.geometry point_geom, b.geometry link_geom FROM rd_electroniceye  a left join rd_link b on a.link_pid = b.link_pid WHERE sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' AND a.u_record != 2";

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

				snapshot.setT(26);

				snapshot.setI(resultSet.getString("pid"));

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("direct"));

				double angle = calAngle(resultSet);

				m.put("b", angle);

				snapshot.setM(m);

				STRUCT struct = (STRUCT) resultSet.getObject("point_geom");

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

	// 计算角度
	private double calAngle(ResultSet resultSet) throws Exception {

		double angle = 0;

		STRUCT struct2 = (STRUCT) resultSet.getObject("link_geom");

		if (struct2 == null) {
			return angle;
		}

		STRUCT struct1 = (STRUCT) resultSet.getObject("point_geom");

		JGeometry geom1 = JGeometry.load(struct1);

		double[] point = geom1.getFirstPoint();

		JGeometry geom2 = JGeometry.load(struct2);

		int ps = geom2.getNumPoints();

		int startIndex = 0;

		for (int i = 0; i < ps - 1; i++) {
			double sx = geom2.getOrdinatesArray()[i * 2];

			double sy = geom2.getOrdinatesArray()[i * 2 + 1];

			double ex = geom2.getOrdinatesArray()[(i + 1) * 2];

			double ey = geom2.getOrdinatesArray()[(i + 1) * 2 + 1];

			if (isBetween(sx, ex, point[0]) && isBetween(sy, ey, point[1])) {
				startIndex = i;
				break;
			}
		}

		StringBuilder sb = new StringBuilder("LINESTRING (");

		sb.append(geom2.getOrdinatesArray()[startIndex * 2]);

		sb.append(" ");

		sb.append(geom2.getOrdinatesArray()[startIndex * 2 + 1]);

		sb.append(", ");

		sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2]);

		sb.append(" ");

		sb.append(geom2.getOrdinatesArray()[(startIndex + 1) * 2 + 1]);

		sb.append(")");

		angle = DisplayUtils.calIncloudedAngle(sb.toString(), resultSet.getInt("direct"));

		return angle;

	}

	private static boolean isBetween(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}

	public static void main(String[] args) throws Exception {
		String wkt = MercatorProjection.getWktWithGap(107953, 49615, 17, 80);
		System.out.println(wkt);
	}
}
