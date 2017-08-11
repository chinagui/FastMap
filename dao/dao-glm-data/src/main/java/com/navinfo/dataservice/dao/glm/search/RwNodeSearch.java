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
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;

import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RwNodeSearch implements ISearch {

	private Connection conn;

	public RwNodeSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RwNodeSelector selector = new RwNodeSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}
	
	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {
		
		RwNodeSelector selector = new RwNodeSelector(conn);		

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, FORM, GEOMETRY FROM RW_NODE WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(A.KIND, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINK_KINDS, LISTAGG(A.FORM, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINK_FORMS FROM RW_LINK A, TMP1 B WHERE A.U_RECORD != 2 AND (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) SAMNODEPART FROM TMP1 B LEFT JOIN RD_SAMENODE_PART A ON B.NODE_PID = A.NODE_PID AND A.TABLE_NAME = 'RW_NODE' AND A.U_RECORD != 2 GROUP BY B.NODE_PID, A.GROUP_ID)  SELECT A.NODE_PID, A.GEOMETRY, A.FORM, B.LINKPIDS, B.LINK_FORMS, B.LINK_KINDS, C.SAMNODEPART FROM TMP1 A, TMP2 B, TMP3 C WHERE A.NODE_PID = B.NODE_PID AND A.NODE_PID = C.NODE_PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, wkt);

			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				String linkPids = resultSet.getString("linkpids");

				String forms = resultSet.getString("link_forms");

				String linkPidArray[] = linkPids.split(",");

				JSONArray linkArray = new JSONArray();

				for (int i = 0; i < linkPids.split(",").length; i++) {
					String linkPid = linkPidArray[i];

					JSONObject linkJSON = new JSONObject();

					linkJSON.put("linkPid", linkPid);

					linkJSON.put("forms", forms.split(",")[i]);

					linkJSON.put("kinds", resultSet.getString("LINK_KINDS").split(",")[i]);

					linkArray.add(linkJSON);
				}

				m.put("a", linkArray);

				String samNodePid = resultSet.getString("samNodePart");

				if (samNodePid != null) {
					m.put("sameNode", samNodePid);
				} else {
					// 0代表没有同一点关系
					m.put("sameNode", 0);
				}

				//rwnode与CRF交叉点无关，但web要求返回格式统一需要加上，以后可与web一起优化。
				m.put("interNode", 0);

				m.put("form", resultSet.getString("FORM"));

				snapshot.setM(m);

				snapshot.setT(22);

				snapshot.setI(resultSet.getInt("node_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (

				Exception e) {

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select node_pid, geometry     from rw_node    where sdo_relate(geometry, sdo_geometry(    :1 , 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2),  tmp2 as (      select /*+ index(a) */    b.node_pid, listagg(a.link_pid, ',') within group(order by b.node_pid) linkpids     from rw_link a, tmp1 b    where a.u_record != 2      and (a.s_node_pid=b.node_pid or a.e_node_pid=b.node_pid)    group by b.node_pid)    select a.node_pid,a.geometry,b.linkpids from tmp1 a, tmp2 b where a.node_pid = b.node_pid";
		
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

				m.put("a", resultSet.getString("linkpids"));

				snapshot.setM(m);

				snapshot.setT(22);

				snapshot.setI(resultSet.getInt("node_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

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

	public static void main(String[] args) throws Exception {
		
		Connection conn = DBConnector.getInstance().getConnectionById(11);
		
		RwNodeSearch s = new RwNodeSearch(conn);
		
		IObj obj = s.searchDataByPid(132837);
		
		System.out.println(obj.Serialize(null));
	}
}
