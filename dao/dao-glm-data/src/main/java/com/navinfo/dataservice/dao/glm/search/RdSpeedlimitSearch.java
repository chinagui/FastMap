package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class RdSpeedlimitSearch implements ISearch {

	private Connection conn;
	
	String queryType="";

	public RdSpeedlimitSearch(Connection conn) {
		this.conn = conn;
	}
	
	public RdSpeedlimitSearch(Connection conn, String queryType) {
		this.conn = conn;
		this.queryType = queryType;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
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
		
		if (queryType.equals("DEPENDENT")) {
			return handleCondition(x, y, z, gap);
		}		

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT a.pid, a.link_pid, a.speed_type, a.direct, a.capture_flag, a.speed_flag, a.speed_value, a.lane_speed_value, a.speed_dependent,b.geometry link_geom,a.geometry point_geom,a.descript FROM rd_speedlimit  a left join rd_link b on a.link_pid = b.link_pid WHERE sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' AND a.u_record != 2 and a.speed_type IN (0,3,4)";

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

				snapshot.setI(resultSet.getInt("pid"));

				snapshot.setT(6);

				int speedType = resultSet.getInt("speed_type");

				int captureFlag = resultSet.getInt("capture_flag");

				int speedFlag = resultSet.getInt("speed_flag");

				int speedValue = resultSet.getInt("speed_value");

				String laneSpeedValue = resultSet.getString("lane_speed_value");

				int speedDependent = resultSet.getInt("speed_dependent");

				jsonM.put("a", String.valueOf(speedType));

				StringBuilder sb = new StringBuilder();

				if (speedType == 0) {

					sb.append(captureFlag);

					sb.append("|");

					sb.append(speedFlag);

					sb.append("|");

					sb.append(speedValue / 10);

				} else if (speedType == 3) {
					sb.append(speedFlag);

					sb.append("|");

					sb.append(speedValue / 10);

					sb.append("|");

					sb.append(speedDependent);

				} else {
					if (!StringUtils.isEmpty(laneSpeedValue)) {
						String[] lanes = laneSpeedValue.split("\\|");

						sb.append(speedValue / 10);

						for (int i = 0; i < lanes.length; i++) {
							if (i == 0) {
								sb.append(",");
							}

							if (i != 0) {
								sb.append("|");
							}
							sb.append(Integer.valueOf(lanes[i]) / 10);
						}
					}

				}
				jsonM.put("b", sb.toString());

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				double angle = calAngle(resultSet);

				jsonM.put("c", String.valueOf((int) angle));

				snapshot.setG(Geojson.lonlat2Pixel(geom2.getFirstPoint()[0],
						geom2.getFirstPoint()[1], z, px, py));

				jsonM.put("d", resultSet.getInt("direct"));

				jsonM.put("e", resultSet.getInt("link_pid"));

				jsonM.put("f", resultSet.getString("descript") == null ? ""
						: resultSet.getString("descript"));

				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			DbUtils.close(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return list;
	}
	
	

	private List<SearchSnapshot> handleCondition(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "SELECT a.pid, a.limit_src,a.speed_value, a.speed_dependent,a.direct, a.link_pid, b.geometry link_geom,a.geometry point_geom FROM rd_speedlimit  a left join rd_link b on a.link_pid = b.link_pid WHERE sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' AND a.u_record != 2 and a.speed_type=3  ORDER BY A.LINK_PID";

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

				snapshot.setI(resultSet.getInt("pid"));

				snapshot.setT(51);
				
				int limitSrc = resultSet.getInt("limit_src");

				int speedValue = resultSet.getInt("speed_value");

				int speedDependent = resultSet.getInt("speed_dependent");

				int direct = resultSet.getInt("direct");

				int linkPid = resultSet.getInt("link_pid");

				jsonM.put("a", String.valueOf(linkPid));				
				jsonM.put("b", String.valueOf(limitSrc));
				jsonM.put("c", String.valueOf(speedValue));
				jsonM.put("d", String.valueOf(speedDependent));
				jsonM.put("e", String.valueOf(direct));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				double angle = calAngle(resultSet);

				jsonM.put("f", String.valueOf((int) angle));
				
				snapshot.setM(jsonM);

				snapshot.setG(Geojson.lonlat2Pixel(geom2.getFirstPoint()[0],
						geom2.getFirstPoint()[1], z, px, py));

				list.add(snapshot);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			DbUtils.close(resultSet);
			DbUtils.closeQuietly(pstmt);

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

		angle = DisplayUtils.calIncloudedAngle(sb.toString(),
				resultSet.getInt("direct"));

		return angle;

	}

	private static boolean isBetween(double a, double b, double c) {
		return b > a ? c >= a && c <= b : c >= b && c <= a;
	}

	// 通过传入点限速的LINKPID和通行方向，返回跟踪LINK路径
	public String trackSpeedLimitLink(int linkPid, int direct) throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(conn);

		return selector.trackSpeedLimitLink(linkPid, direct);
	}

	public static void main(String[] args) throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		RdSpeedlimitSearch a = new RdSpeedlimitSearch(conn);

		System.out.println(JSONArray.fromObject(a.searchDataByTileWithGap(
				107951, 49621, 17, 20)));

	}
}