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
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: RdInterSearch 
* @author Zhang Xiaolong
* @date 2016年8月3日 下午2:11:39 
*/
public class RdInterSearch implements ISearch {
	
	private Connection conn;
	
	public RdInterSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return (IObj) new AbstractSelector(RdInter.class, conn).loadById(pid, false);
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
		
		String sql = "WITH tmp1 AS (	SELECT node_pid FROM rd_node WHERE sdo_relate(geometry, sdo_geometry( :1, 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2), tmp2 as（ SELECT /*+ index(b) */ pid, listagg(a.node_pid, ',') within group( ORDER BY a.node_pid) node_pids, listagg(sdo_util.to_wktgeometry_varchar(b. geometry), ',') within group( ORDER BY a.node_pid) node_wkts FROM rd_inter_node a, rd_node b WHERE EXISTS (	SELECT NULL FROM tmp1 C WHERE a.node_pid = C.node_pid) AND a.node_pid = b.node_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.pid）, tmp3 AS (	SELECT link_pid FROM rd_link WHERE sdo_relate(geometry, sdo_geometry(:2, 8307), 'mask=anyinteract') = 'TRUE' AND u_record != 2), tmp4 as（ SELECT /*+ index(b) */ pid, listagg(a.link_pid, ',') within group( ORDER BY a.link_pid) link_pids,listagg(b.s_node_pid, ',') within group( ORDER BY a.link_pid) s_node_pids,listagg(b.e_node_pid, ',') within group( ORDER BY a.link_pid) e_node_pids, listagg(sdo_util.to_wktgeometry_varchar(b. geometry), ';') within group( ORDER BY a.link_pid) link_wkts FROM rd_inter_link a, rd_link b WHERE EXISTS (	SELECT NULL FROM tmp3 C WHERE a.link_pid = C.link_pid) AND a.link_pid = b.link_pid AND a.u_record != 2 AND b.u_record != 2 GROUP BY a.pid） SELECT tmp2.*,tmp4.link_pids,tmp4.link_wkts,s_node_pids,e_node_pids FROM tmp2 LEFT JOIN tmp4 ON tmp2.pid = tmp4.pid";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		WKTReader wktReader = new WKTReader();

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
			
			System.out.println(wkt);

			pstmt.setString(1, wkt);
			
			pstmt.setString(2, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));

				snapshot.setT(39);

				String nodePids = resultSet.getString("node_pids");

				String []splits = nodePids.split(",");

				String wktPoints = resultSet.getString("node_wkts");

				JSONArray gArray = new JSONArray();

				String []nodeWktSplits = wktPoints.split(",");

				for (int i = 0; i < splits.length; i++) {
					JSONObject gObject = new JSONObject();

					Geometry gNode = wktReader.read(nodeWktSplits[i]);

						gObject.put("g", Geojson.lonlat2Pixel(
								gNode.getCoordinate().x,
								gNode.getCoordinate().y, z, px, py));
						gObject.put("i", splits[i]);
						
						gArray.add(gObject);
				}

				snapshot.setG(gArray);
				
				JSONObject jsonM = new JSONObject();
				
				String linkPids = resultSet.getString("link_pids");
				
				if(StringUtils.isNotEmpty(linkPids))
				{
					String []linkSplits = linkPids.split(",");

					String wktLinks = resultSet.getString("link_wkts");

					JSONArray gLinkArray = new JSONArray();

					String []linkWktSplits = wktLinks.split(";");
					
					String sNodePids[] = resultSet.getString("s_node_pids").split(",");
					
					String eNodePids[] = resultSet.getString("e_node_pids").split(",");

					for (int i = 0; i < linkSplits.length; i++) {
						JSONObject gObject = new JSONObject();
						
						JSONObject geojson = Geojson.wkt2Geojson(linkWktSplits[i]);

						JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

							gObject.put("g", jo.getJSONArray("coordinates"));
							gObject.put("i", linkSplits[i]);
							gObject.put("s", sNodePids[i]);
							gObject.put("e", eNodePids[i]);
							
							gLinkArray.add(gObject);
					}
					
					jsonM.put("a", gLinkArray);
					
					snapshot.setM(jsonM);
				}

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

}
