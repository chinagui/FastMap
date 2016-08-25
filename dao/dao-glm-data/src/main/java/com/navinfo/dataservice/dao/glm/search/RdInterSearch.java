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
		
		String sql = "WITH TMP1    AS (	SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1 , 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) , tmp2 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.LINK_PID,A.GEOMETRY FROM TMP1 A, RD_ROAD_LINK B,Rd_object_road C WHERE A.LINK_PID = B.LINK_PID AND C.ROAD_PID = b.pid AND B.U_RECORD != 2 ), tmp3 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ b.PID, A.LINK_PID,A.GEOMETRY FROM TMP1 A, Rd_object_link b WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 ), TMP4 AS (	SELECT node_pid, GEOMETRY FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2 , 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) , tmp5 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.NODE_PID,A.GEOMETRY FROM TMP4 A, RD_INTER_NODE B,Rd_object_INTER C WHERE A.NODE_PID = B.NODE_PID AND C.inter_PID = b.pid AND B.U_RECORD != 2 ) select * from( SELECT tmp6.pid,tmp6.link_pid,sdo_util.to_wktgeometry_varchar(tmp6.GEOMETRY) AS linkGeo,tmp5.node_pid,sdo_util.to_wktgeometry_varchar( tmp5. geometry) AS nodeGeo FROM(	SELECT * FROM tmp2 UNION ALL 	SELECT * FROM tmp3) tmp6 LEFT JOIN tmp5 ON tmp6.pid = tmp5.pid) GROUP BY pid,link_pid,linkGeo,node_pid,nodeGeo";
		
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
