package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class RdLaneSearch implements ISearch {

	private Connection conn;

	public RdLaneSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdLaneSelector selector = new RdLaneSelector(conn);
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

		String sql = "select rk.link_pid, rk.geometry,rk.lane_num,(SELECT count(0) FROM rd_lane_topo_detail rt WHERE    rt.in_link_pid = rk.link_pid OR rt.out_link_pid = rk.link_pid) AS COUNT     from rd_link rk   where sdo_relate(rk.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2 and exists (select  rl.link_pid from rd_lane rl where rk.link_pid = rl.link_pid and rl.u_record != 2) ";

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

				m.put("a", resultSet.getString("lane_num"));
				if (resultSet.getInt("count") > 0) {
					m.put("b", "Y");
				} else {
					m.put("b", "N");
				}

				snapshot.setM(m);

				snapshot.setT(46);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JGeometry geo = JGeometry.load(struct);

				if (geo.getType() != 2) {
					continue;
				}

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

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

		RdLaneSearch s = new RdLaneSearch(conn);

		List<SearchSnapshot> ss = s.searchDataByTileWithGap(107939, 49614, 17,
				1);

		for (SearchSnapshot n : ss) {
			System.out.println(n.Serialize(null));
		}
	}

}
