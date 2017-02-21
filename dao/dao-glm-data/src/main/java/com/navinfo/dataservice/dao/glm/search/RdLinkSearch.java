package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdLinkSearch implements ISearch {

	private Connection conn;

	public RdLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		IObj link = (IObj) linkSelector.loadById(pid, false);

		return link;
	}

	@Override
	public List<? extends IObj> searchDataByPids(List<Integer> pidList)
			throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<RdLink> linkList = linkSelector.loadByPids(pidList, false);

		return linkList;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT A.LINK_PID, A.KIND, A.GEOMETRY, A.S_NODE_PID, A.E_NODE_PID FROM RD_LINK A WHERE SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND A.U_RECORD != 2), TMP2 AS /*+ index(P) */ (SELECT P.LINK_PID, S.GROUP_ID SAMELINK_PID FROM RD_SAMELINK_PART P, RD_SAMELINK S, TMP1 L WHERE P.LINK_PID = L.LINK_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :2 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP3 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID S_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.S_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :3 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP4 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID E_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.E_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME = :4 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2) SELECT A.*, B.SAMELINK_PID, C.S_SAMENODEPID, D.E_SAMENODEPID FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D WHERE A.LINK_PID = B.LINK_PID(+) AND A.LINK_PID = C.LINK_PID(+) AND A.LINK_PID = D.LINK_PID(+)";
		
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);
			pstmt.setString(2, "RD_LINK");
			pstmt.setString(3, "RD_NODE");
			pstmt.setString(4, "RD_NODE");

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("s_node_pid"));

				m.put("b", resultSet.getString("e_node_pid"));
				
				m.put("c", resultSet.getString("kind"));
				
				m.put("d", resultSet.getInt("samelink_pid"));
				
				m.put("e", resultSet.getInt("s_samenodepid"));
				
				m.put("f", resultSet.getInt("e_samenodepid"));

				snapshot.setM(m);

				snapshot.setT(12);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject jo = Geojson.spatial2Geojson(struct);

				snapshot.setG(jo.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			DBUtils.closeStatement(pstmt);
			DBUtils.closeResultSet(resultSet);
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

		String sql = "with tmp1 as  (select link_pid,direct, kind,special_traffic,function_class, s_node_pid, e_node_pid,length,imi_code, geometry     from rd_link    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2), tmp2 as  (select /*+ index(a) */    a.link_pid, listagg(a.type, ';') within group(order by a.link_pid) limits     from rd_link_limit a, tmp1 b    where a.u_record != 2      and a.link_pid = b.link_pid    group by a.link_pid), tmp3 as  (select /*+ index(a) */    a.link_pid,    listagg(a.form_of_way, ';') within group(order by a.link_pid) forms     from rd_link_form a, tmp1 b    where a.u_record != 2      and a.link_pid = b.link_pid    group by a.link_pid) select a.*, b.limits, c.forms,d.name   from tmp1 a, tmp2 b, tmp3 c, (select /*+ index(b) */            b.link_pid, c.name             from rd_link_name b, rd_name c            where b.name_groupid = c.name_groupid              and b.name_class = 1              and b.seq_num = 1              and c.lang_code = 'CHI'              and b.u_record != 2) d  where a.link_pid = b.link_pid(+)    and a.link_pid = c.link_pid(+)    and a.link_pid = d.link_pid(+)";

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

				m.put("a", resultSet.getString("kind"));

				m.put("b", resultSet.getString("name"));

				m.put("c", resultSet.getString("limits"));

				m.put("d", resultSet.getString("direct"));

				m.put("e", resultSet.getString("s_node_pid"));

				m.put("f", resultSet.getString("e_node_pid"));

				m.put("h", resultSet.getString("forms"));

				m.put("i", resultSet.getString("function_class"));

				m.put("j", resultSet.getString("imi_code"));
				m.put("k", resultSet.getString("length"));
				
				m.put("l", resultSet.getString("special_traffic"));

				snapshot.setM(m);

				snapshot.setT(4);

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

		RdLinkSearch a = new RdLinkSearch(conn);

		System.out.println(JSONArray.fromObject(a.searchDataByTileWithGap(
				107951, 49621, 17, 20)));

	}
}
