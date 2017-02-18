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
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class AdLinkSearch implements ISearch {

	private Connection conn;

	public AdLinkSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		AdLinkSelector adLinkSelector = new AdLinkSelector(conn);

		IObj adLink = (IObj) adLinkSelector.loadById(pid, false);

		return adLink;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT A.LINK_PID, A.KIND, A.GEOMETRY, A.S_NODE_PID, A.E_NODE_PID FROM AD_LINK A WHERE SDO_WITHIN_DISTANCE(A.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND A.U_RECORD != 2), TMP2 AS /*+ index(P) */ (SELECT P.LINK_PID, S.GROUP_ID SAMELINK_PID FROM RD_SAMELINK_PART P, RD_SAMELINK S, TMP1 L WHERE P.LINK_PID = L.LINK_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME=:2 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP3 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID S_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.S_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME=:3 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2), TMP4 AS /*+ index(P) */ (SELECT L.LINK_PID, S.GROUP_ID E_SAMENODEPID FROM RD_SAMENODE_PART P, RD_SAMENODE S, TMP1 L WHERE P.NODE_PID = L.E_NODE_PID AND S.GROUP_ID = P.GROUP_ID AND P.TABLE_NAME=:4 AND P.U_RECORD <> 2 AND S.U_RECORD <> 2) SELECT A.*, B.SAMELINK_PID, C.S_SAMENODEPID, D.E_SAMENODEPID FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D WHERE A.LINK_PID = B.LINK_PID(+) AND A.LINK_PID = C.LINK_PID(+) AND A.LINK_PID = D.LINK_PID(+)";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);
			pstmt.setString(2, "AD_LINK");
			pstmt.setString(3, "AD_NODE");
			pstmt.setString(4, "AD_NODE");

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

		String sql = "select a.link_pid,        a.geometry,        a.kind,  a.s_node_pid,        a.e_node_pid   from ad_link a          where a.u_record != 2      and sdo_within_distance(a.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') =        'TRUE'";

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

				m.put("a", resultSet.getString("s_node_pid"));

				m.put("b", resultSet.getString("e_node_pid"));
				
				m.put("c", resultSet.getInt("kind"));

				snapshot.setM(m);

				snapshot.setT(12);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				JGeometry geo = JGeometry.load(struct);
				
				if(geo.getType()!=2){
					continue;
				}
				
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
		
		AdLinkSearch search = new AdLinkSearch(conn);
		
		List<SearchSnapshot> res = search.searchDataByTileWithGap(215829, 99329, 18, 20);
		
		for(SearchSnapshot s : res){
			System.out.println(s.Serialize(null));
		}
	}
}