package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;


/**
 * 
 * @author luyao
 *
 */
public class ZoneNodeSearch implements ISearch {
	
	private Connection conn;

	public ZoneNodeSearch(Connection conn) {
		super();
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		ZoneNodeSelector zoneNodeSelector = new ZoneNodeSelector(conn);

		IObj node = (IObj) zoneNodeSelector.loadById(pid, false);

		return node;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT NODE_PID, GEOMETRY, FORM FROM ZONE_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT /*+ index(a) index(c)*/ B.NODE_PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) LINKPIDS, LISTAGG(C.KIND, ',') WITHIN GROUP(ORDER BY B.NODE_PID) KINDS FROM TMP1 B LEFT JOIN ZONE_LINK A ON (A.S_NODE_PID = B.NODE_PID OR A.E_NODE_PID = B.NODE_PID) AND A.U_RECORD != 2 LEFT JOIN ZONE_LINK_KIND C ON A.LINK_PID = C.LINK_PID AND C.U_RECORD != 2 GROUP BY B.NODE_PID), TMP3 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.GROUP_ID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) SAMNODEPART FROM TMP1 B LEFT JOIN RD_SAMENODE_PART A ON B.NODE_PID = A.NODE_PID AND A.TABLE_NAME = 'LU_NODE' GROUP BY B.NODE_PID, A.GROUP_ID), TMP4 AS (SELECT /*+ index(a) */ B.NODE_PID, LISTAGG(A.PID, ',') WITHIN GROUP(ORDER BY B.NODE_PID) AS INTERNODE FROM TMP1 B LEFT JOIN RD_INTER_NODE A ON B.NODE_PID = A.NODE_PID GROUP BY B.NODE_PID) SELECT A.NODE_PID, A.GEOMETRY, A.FORM, B.LINKPIDS, B.KINDS, C.SAMNODEPART, D.INTERNODE FROM TMP1 A, TMP2 B, TMP3 C, TMP4 D WHERE A.NODE_PID = B.NODE_PID AND B.NODE_PID = C.NODE_PID AND D.NODE_PID = C.NODE_PID ";

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

				String kinds = resultSet.getString("kinds");

				Map<String, JSONObject> linkMap = new HashMap<>();

				String linkPidArray[] = linkPids.split(",");

				for (int i = 0; i < linkPids.split(",").length; i++) {
					String linkPid = linkPidArray[i];

					JSONObject linkJSON = linkMap.get(linkPid);

					if (linkJSON != null) {
						String kind = linkJSON.getString("kinds");
						linkJSON.element("kinds", kind +","+kinds.split(",")[i]);
					} else {
						linkJSON = new JSONObject();

						linkJSON.put("linkPid", linkPid);

						linkJSON.put("kinds", kinds.split(",")[i]);
						
						String samNodePid = resultSet.getString("samNodePart");

						if (samNodePid != null) {
							linkJSON.put("sameNode", Integer.parseInt(samNodePid));
						} else {
							// 0代表没有同一点关系
							linkJSON.put("sameNode", 0);
						}

						String interNodePid = resultSet.getString("interNode");

						if (interNodePid != null) {
							linkJSON.put("interNode", Integer.parseInt(interNodePid));
						} else {
							// 0代表没有制作CRFI
							linkJSON.put("interNode", 0);
						}
						linkMap.put(linkPid, linkJSON);
					}
				}

				m.put("a", linkMap.values());
				
				//点的形态
				m.put("b", resultSet.getInt("form"));

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

		String sql = "with tmp1 as  (select node_pid, geometry     from zone_node    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2), tmp2 as  (select /*+ index(a) */    b.node_pid,    listagg(a.link_pid, ',') within group(order by b.node_pid) linkpids     from zone_link a, tmp1 b    where a.u_record != 2      and (a.s_node_pid = b.node_pid or a.e_node_pid = b.node_pid)    group by b.node_pid) select a.node_pid, a.geometry, b.linkpids   from tmp1 a, tmp2 b  where a.node_pid = b.node_pid";
		
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

				snapshot.setT(20);

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
