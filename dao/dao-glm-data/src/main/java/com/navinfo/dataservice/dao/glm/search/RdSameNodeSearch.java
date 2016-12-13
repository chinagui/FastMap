/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: RdSameNodeSearch
 * @author Zhang Xiaolong
 * @date 2016年8月8日 下午4:59:41
 * @Description: TODO
 */
public class RdSameNodeSearch implements ISearch {

	private Connection conn;

	public RdSameNodeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return (IObj) new AbstractSelector(RdSameNode.class, conn).loadById(pid, false);
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH tmp1 AS (	SELECT node_pid FROM rd_node WHERE sdo_relate(geometry, sdo_geometry( :1 , 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2) SELECT /*+ index(b) */ a.group_id, listagg(a.node_pid, ',') within group( ORDER BY a.node_pid) node_pids, listagg(sdo_util.to_wktgeometry_varchar(b. geometry), ',') within group( ORDER BY a.node_pid) node_wkts FROM rd_samenode_part a, rd_node b WHERE EXISTS (	SELECT NULL FROM tmp1 C WHERE a.node_pid = C.node_pid AND a.table_name = 'RD_NODE') AND a.node_pid = b.node_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.group_id UNION ALL SELECT * FROM( WITH tmp2 AS (SELECT node_pid FROM ad_node WHERE sdo_relate(geometry, sdo_geometry( :2 , 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2) 	SELECT /*+ index(b) */ a.group_id, listagg(a.node_pid, ',') within group( ORDER BY a.node_pid) node_pids, listagg( sdo_util.to_wktgeometry_varchar(b. geometry), ',') within group( ORDER BY a.node_pid) node_wkts FROM rd_samenode_part a, ad_node b WHERE EXISTS (	SELECT NULL FROM tmp2 C WHERE a.node_pid = C.node_pid AND a.table_name = 'AD_NODE') AND a.node_pid = b.node_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.group_id) UNION ALL SELECT * FROM( WITH tmp3 AS (SELECT node_pid FROM lu_node WHERE sdo_relate(geometry, sdo_geometry( :3 , 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2) 	SELECT /*+ index(b) */ a.group_id, listagg(a.node_pid, ',') within group( ORDER BY a.node_pid) node_pids, listagg( sdo_util.to_wktgeometry_varchar(b. geometry), ',') within group( ORDER BY a.node_pid) node_wkts FROM rd_samenode_part a, lu_node b WHERE EXISTS (	SELECT NULL FROM tmp3 C WHERE a.node_pid = C.node_pid AND a.table_name = 'LU_NODE') AND a.node_pid = b.node_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.group_id) UNION ALL SELECT * FROM( WITH tmp4 AS (SELECT node_pid FROM zone_node WHERE sdo_relate(geometry, sdo_geometry( :4 , 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2) 	SELECT /*+ index(b) */ a.group_id, listagg(a.node_pid, ',') within group( ORDER BY a.node_pid) node_pids, listagg( sdo_util.to_wktgeometry_varchar(b. geometry), ',') within group( ORDER BY a.node_pid) node_wkts FROM rd_samenode_part a, zone_node b WHERE EXISTS (	SELECT NULL FROM tmp4 C WHERE a.node_pid = C.node_pid AND UPPER(a.table_name) = 'ZONE_NODE') AND a.node_pid = b.node_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.group_id) ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		WKTReader wktReader = new WKTReader();

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);
			pstmt.setString(2, wkt);
			pstmt.setString(3, wkt);
			pstmt.setString(4, wkt);

			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			List<Integer> groupIdList = new ArrayList<>();

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject jsonM = new JSONObject();

				int groupId = resultSet.getInt("group_id");

				if (!groupIdList.contains(groupId)) {
					
					groupIdList.add(groupId);
					
					snapshot.setI(groupId);

					snapshot.setT(37);

					String wktPoints = resultSet.getString("node_wkts");

					String[] splits = wktPoints.split(",");

					if (splits.length > 0) {
						Geometry gNode = wktReader.read(splits[0]);

						snapshot.setG(Geojson.lonlat2Pixel(gNode.getCoordinate().x, gNode.getCoordinate().y, z, px, py));
					}

					String nodePids = resultSet.getString("node_pids");

					JSONArray nodePidArray = new JSONArray();

					String[] nodeSplits = nodePids.split(",");

					for (int i = 0; i < nodeSplits.length; i++) {

						nodePidArray.add(nodeSplits[i]);
					}

					jsonM.put("a", nodePidArray);

					snapshot.setM(jsonM);
					
					list.add(snapshot);
				}
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

}
