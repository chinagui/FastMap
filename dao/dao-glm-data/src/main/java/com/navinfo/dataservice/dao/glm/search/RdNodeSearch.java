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
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdNodeSearch implements ISearch {

	private Connection conn;

	public RdNodeSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdNodeSelector selector = new RdNodeSelector(conn);
		
		IObj obj = (IObj)selector.loadById(pid, false);
		
		return obj;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(A.IMI_CODE, ',') WITHIN GROUP(ORDER BY B.NODE_PID) IMICODES, LISTAGG(c.form_of_way, ',') WITHIN GROUP(ORDER BY B.NODE_PID) link_forms FROM RD_LINK A, TMP1 B,Rd_Link_Form c WHERE A.U_RECORD != 2 AND (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) AND a.link_pid = c.link_pid GROUP BY B.NODE_PID), tmp3 as ( select /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) samNodePart from tmp1 B left join RD_SAMENODE_PART A on B.NODE_PID = a.NODE_PID GROUP BY B.NODE_PID,A.GROUP_ID ), tmp4 as ( select /*+ index(a) */ B.NODE_PID, LISTAGG(A.pid, ',') WITHIN GROUP(ORDER BY B.NODE_PID) as interNode from tmp1 B left join RD_INTER_NODE A on B.NODE_PID = a.NODE_PID GROUP BY B.NODE_PID ) SELECT A.NODE_PID, A.GEOMETRY, B.LINKPIDS,B.link_forms,B.IMICODES,c.samNodePart,d.interNode FROM TMP1 A, TMP2 B,tmp3 C,tmp4 D WHERE A.NODE_PID = B.NODE_PID and b.node_pid = c.node_pid and d.node_pid = c.node_pid ";
		
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

				m.put("a", resultSet.getString("linkpids"));
				
				m.put("b", resultSet.getString("link_forms"));
				
				m.put("c", resultSet.getString("IMICODES"));
				
				m.put("d", resultSet.getString("samNodePart"));
				
				m.put("e", resultSet.getString("interNode"));

				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(resultSet.getString("node_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

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

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select node_pid, geometry     from rd_node    where sdo_relate(geometry, sdo_geometry(    :1 , 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2),  tmp2 as (      select /*+ index(a) */    b.node_pid, listagg(a.link_pid, ',') within group(order by b.node_pid) linkpids     from rd_link a, tmp1 b    where a.u_record != 2      and (a.s_node_pid=b.node_pid or a.e_node_pid=b.node_pid)    group by b.node_pid)    select a.node_pid,a.geometry,b.linkpids from tmp1 a, tmp2 b where a.node_pid = b.node_pid";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);
			
			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getString("linkpids"));

				snapshot.setM(m);

				snapshot.setT(16);

				snapshot.setI(resultSet.getString("node_pid"));

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
}
