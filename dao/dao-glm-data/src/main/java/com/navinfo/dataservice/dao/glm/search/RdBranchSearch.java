package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;

public class RdBranchSearch implements ISearch {

	private Connection conn;

	public RdBranchSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdBranchSelector selector = new RdBranchSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

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

		String sql = "with tmp1 as  (select link_pid, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE' and u_record != 2), tmp2 as  (select /*+ index(a) */ a.in_link_pid,          a.node_pid,          d.branch_type,          listagg(a.branch_pid || '-' || d.detail_id, ',') within group(order by 1) pids     from rd_branch a, tmp1 b, rd_branch_detail d    where a.in_link_pid = b.link_pid      and a.branch_pid = d.branch_pid      and branch_type in (0, 1, 2, 3)      and a.u_record != 2 and d.u_record != 2    group by a.in_link_pid, a.node_pid, d.branch_type), tmp3 as  (select in_link_pid,          node_pid,          listagg(branch_type || '~' || pids, '^') within group(order by branch_type) a     from tmp2    group by in_link_pid, node_pid) select /*+ index(c) */  a.a, b.geometry link_geom, c.geometry point_geom   from tmp3 a, tmp1 b, rd_node c  where a.in_link_pid = b.link_pid    and a.node_pid = c.node_pid    and c.u_record != 2";

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

				JSONArray ja = new JSONArray();

				String[] splits = resultSet.getString("a").split("\\^");

				for (String s1 : splits) {
					String[] s2 = s1.split("\\~");

					JSONObject j = new JSONObject();

					j.put("type", Integer.parseInt(s2[0]));

					String[] s3 = s2[1].split(",");

					JSONArray ja2 = new JSONArray();

					for (String s4 : s3) {
						String[] s5 = s4.split("\\-");

						JSONObject j2 = new JSONObject();

						j2.put("branchPid", Integer.parseInt(s5[0]));

						j2.put("detailId", Integer.parseInt(s5[1]));

						ja2.add(j2);
					}

					j.put("ids", ja2);

					ja.add(j);
				}

				jsonM.put("a", ja);

				STRUCT struct1 = (STRUCT) resultSet.getObject("link_geom");

				JGeometry geom1 = JGeometry.load(struct1);

				String linkWkt = new String(new WKT().fromJGeometry(geom1));

				STRUCT struct2 = (STRUCT) resultSet.getObject("point_geom");

				JGeometry geom2 = JGeometry.load(struct2);

				String pointWkt = new String(new WKT().fromJGeometry(geom2));

				int direct = DisplayUtils.getDirect(linkWkt, pointWkt);

				double angle = DisplayUtils.calIncloudedAngle(linkWkt, direct);

				jsonM.put("c", String.valueOf((int) angle));

				double[][] point = DisplayUtils.getGdbPointPos(linkWkt,
						pointWkt, 2);

				snapshot.setG(Geojson.lonlat2Pixel(point[1][0], point[1][1], z,
						px, py));

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

	public static void main(String[] args) throws Exception {
		Connection conn = GlmDbPoolManager.getInstance().getConnection(11);

		RdBranchSearch s = new RdBranchSearch(conn);

		// IObj obj = s.searchDataByPid(3495);
		//
		// System.out.println(obj.Serialize(null));

		System.out.println(s.searchDataByTileWithGap(107943, 49614, 17, 20));
	}
}