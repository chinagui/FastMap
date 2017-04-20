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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

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
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {
		
		RdInterSelector selector = new RdInterSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
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
		
		String sql = "WITH TMP11 AS (SELECT NODE_PID FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP12 AS (SELECT LINK_PID FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP1 AS (SELECT /*+ index(a) */ A.PID INTER_PID FROM RD_INTER_NODE A, TMP11 B WHERE B.NODE_PID = A.NODE_PID AND A.U_RECORD != 2 UNION SELECT /*+ index(a) */ A.PID INTER_PID FROM RD_INTER_LINK A, TMP12 B WHERE B.LINK_PID = A.LINK_PID AND A.U_RECORD != 2), TMP2 AS (SELECT /*+ index(b) */ PID, LISTAGG(A.NODE_PID, ',') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_PIDS, LISTAGG(SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(B. GEOMETRY), ',') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_WKTS FROM RD_INTER_NODE A, RD_NODE B WHERE EXISTS (SELECT NULL FROM TMP1 C WHERE A.PID = C.INTER_PID) AND A.NODE_PID = B.NODE_PID AND A.U_RECORD != 2 AND B.U_RECORD != 2 GROUP BY A.PID), TMP3 AS (SELECT /*+ index(b) */ PID, LISTAGG(A.LINK_PID, ',') WITHIN GROUP(ORDER BY A.LINK_PID) LINK_PIDS, LISTAGG(B.S_NODE_PID, ',') WITHIN GROUP(ORDER BY A.LINK_PID) S_NODE_PIDS, LISTAGG(B.E_NODE_PID, ',') WITHIN GROUP(ORDER BY A.LINK_PID) E_NODE_PIDS, LISTAGG(SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(B. GEOMETRY), ';') WITHIN GROUP(ORDER BY A.LINK_PID) LINK_WKTS FROM RD_INTER_LINK A, RD_LINK B WHERE EXISTS (SELECT NULL FROM TMP1 C WHERE A.PID = C.INTER_PID) AND A.LINK_PID = B.LINK_PID AND A.U_RECORD != 2 AND B.U_RECORD != 2 GROUP BY A.PID) SELECT TMP2.*, TMP3.LINK_PIDS, TMP3.LINK_WKTS, S_NODE_PIDS, E_NODE_PIDS FROM TMP2 LEFT JOIN TMP3 ON TMP2.PID = TMP3.PID ";
		
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

				snapshot.setI(resultSet.getInt("pid"));

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
							gObject.put("i", Integer.parseInt(linkSplits[i]));
							gObject.put("s", Integer.parseInt(sNodePids[i]));
							gObject.put("e", Integer.parseInt(eNodePids[i]));
							
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
