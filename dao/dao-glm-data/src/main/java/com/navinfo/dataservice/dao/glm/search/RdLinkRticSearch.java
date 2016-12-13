package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdLinkRticSearch implements ISearch {

	private Connection conn;

	public RdLinkRticSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return null;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select link_pid     from rd_link    where sdo_relate(geometry, sdo_geometry(   :1    , 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2), tmp2 as (select listagg(a.code || ',' || a.rank || ',' || a.rtic_dir || ',' ||                        a.updown_flag,                        '-') within group(order by a.link_pid) rtics,               a.link_pid           from rd_link_rtic a, tmp1 b          where a.u_record != 2 and a.link_pid=b.link_pid          group by a.link_pid)               select a.*,b.geometry from tmp2 a , rd_link b where a.link_pid=b.link_pid ";

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

					String updownStr = updownFlag2String(Integer
							.valueOf(updownFlag));

					String info = rankStr + code + updownStr;

					if (dir.equals("1")) {
						m.put("a", info);

						m.put("b", rank);
					} else if (dir.equals("2")) {
						m.put("c", info);

						m.put("d", rank);
					}
				}

				snapshot.setM(m);

				snapshot.setT(45);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
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

		String sql = "with tmp1 as  (select link_pid     from rd_link    where sdo_relate(geometry, sdo_geometry(   :1    , 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2), tmp2 as (select listagg(a.code || ',' || a.rank || ',' || a.rtic_dir || ',' ||                        a.updown_flag,                        '-') within group(order by a.link_pid) rtics,               a.link_pid           from rd_link_rtic a, tmp1 b          where a.u_record != 2 and a.link_pid=b.link_pid          group by a.link_pid)               select a.*,b.geometry from tmp2 a , rd_link b where a.link_pid=b.link_pid ";

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

					String updownStr = updownFlag2String(Integer
							.valueOf(updownFlag));

					String info = rankStr + code + updownStr;

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

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	private String updownFlag2String(int updownFlag) {
		switch (updownFlag) {
		case 0:
			return "上";
		case 1:
			return "下";
		}

		return "";
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
