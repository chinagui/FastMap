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

public class RticSearch implements ISearch {

	private Connection conn;

	public RticSearch(Connection conn) {
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

		String sql = "select a.link_pid,        a.geometry,        b.rtics   from rd_link a,        (select listagg(code || ',' || rank|| ',' ||                                rtic_dir || ',' || updown_flag,                                '-') within group(order by link_pid) rtics,                        link_pid                   from rd_link_rtic where u_record!=2                  group by link_pid) b          where a.link_pid = b.link_pid  and a.u_record != 2     and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				String rticstr = resultSet.getString("rtics");

				String[] rtics = rticstr.split("-");

				for (int i = 0; i < rtics.length; i++) {

					String[] splits = rtics[i].split(",");

					String code = splits[0];

					String rank = splits[1];

					String dir = splits[2];

					String updownFlag = splits[3];

					String rankStr = rank2String(Integer.valueOf(rank));

					String info = rankStr + code + updownFlag;

					if (dir.equals("1")) {
						m.put("a", info);

						m.put("b", rank);
					} else if (dir.equals("2")) {
						m.put("c", info);

						m.put("d", rank);
					}
				}

				snapshot.setM(m);

				snapshot.setT(10);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

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

		String sql = "select a.link_pid,        a.geometry,        b.rtics   from rd_link a,        (select listagg(code || ',' || rank|| ',' ||                                rtic_dir || ',' || updown_flag,                                '-') within group(order by link_pid) rtics,                        link_pid                   from rd_link_rtic where u_record!=2                  group by link_pid) b          where a.link_pid = b.link_pid  and a.u_record != 2     and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

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

				JSONObject m = new JSONObject();

				String rticstr = resultSet.getString("rtics");

				String[] rtics = rticstr.split("-");

				for (int i = 0; i < rtics.length; i++) {

					String[] splits = rtics[i].split(",");

					String code = splits[0];

					String rank = splits[1];

					String dir = splits[2];

					String updownFlag = splits[3];

					String rankStr = rank2String(Integer.valueOf(rank));

					String info = rankStr + code + updownFlag;

					if (dir.equals("1")) {
						m.put("a", info);

						m.put("b", rank);
					} else if (dir.equals("2")) {
						m.put("c", info);

						m.put("d", rank);
					}
				}

				snapshot.setM(m);

				snapshot.setT(10);

				snapshot.setI(String.valueOf(resultSet.getInt("link_pid")));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

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

	private String rank2String(int rank) {

		switch (rank) {
		case 2:
			return "C";
		case 3:
			return "G";
		case 4:
			return "F";
		}

		return "";
	}

}
