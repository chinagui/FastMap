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
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RwLinkSearch extends AbstractSearch implements ISearch {

	private Connection conn;

	public RwLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return searchDataByPid(RwLink.class, pid, conn);
	}
	
	@Override
	public IObj searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.link_pid,        b.name,        a.geometry   from rw_link a,        (select /*+ index(b) */          b.link_pid, c.name           from rw_link_name b, rd_name c          where b.name_groupid = c.name_groupid            and c.lang_code = 'CHI'            and b.u_record != 2) b  where a.u_record != 2    and a.link_pid = b.link_pid(+)    and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("name"));

				snapshot.setM(m);

				snapshot.setT(14);

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
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {

		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "select a.link_pid, a.color, a.s_node_pid ,a.e_node_pid,      b.name,        a.geometry   from rw_link a,        (select /*+ index(b) */          b.link_pid, c.name           from rw_link_name b, rd_name c          where b.name_groupid = c.name_groupid            and c.lang_code = 'CHI'            and b.u_record != 2) b  where a.u_record != 2    and a.link_pid = b.link_pid(+)    and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

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

				m.put("a", resultSet.getString("name"));

				m.put("b", resultSet.getString("color"));

				m.put("c", resultSet.getString("s_node_pid"));

				m.put("d", resultSet.getString("e_node_pid"));

				snapshot.setM(m);

				snapshot.setT(14);

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

}
