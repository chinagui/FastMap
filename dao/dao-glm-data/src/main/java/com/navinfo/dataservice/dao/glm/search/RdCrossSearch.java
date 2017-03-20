package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdCrossSearch implements ISearch {

	private Connection conn;

	public RdCrossSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdCrossSelector selector = new RdCrossSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);

		return obj;
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

		String sql = "WITH TMP1 AS (SELECT distinct b.pid FROM RD_NODE a,RD_CROSS_NODE b WHERE SDO_RELATE(a.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' and a.NODE_PID = b.NODE_PID AND a.U_RECORD != 2 and b.U_RECORD !=2) SELECT /*+ index(b) */ a.PID, LISTAGG(A.NODE_PID, ',') WITHIN GROUP(ORDER BY A.NODE_PID) NODE_PIDS, LISTAGG(SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(B.GEOMETRY), ',') WITHIN GROUP(ORDER BY A.NODE_PID) WKTS, LISTAGG(A.IS_MAIN, ',') WITHIN GROUP(ORDER BY A.NODE_PID) IS_MAINS FROM RD_CROSS_NODE A,tmp1,rd_node b WHERE a.pid = tmp1.pid and a.NODE_PID = b.node_pid and b.U_RECORD !=2 and a.u_record <> 2 GROUP BY A.PID  ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		WKTReader wktReader = new WKTReader();

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

				snapshot.setI(resultSet.getInt("pid"));

				snapshot.setT(8);

				JSONArray maArray = new JSONArray();

				String nodePids = resultSet.getString("node_pids");

				String wktPoints = resultSet.getString("wkts");

				String isMains = resultSet.getString("is_mains");

				String[] nodeSplits = nodePids.split(",");

				for (int i = 0; i < nodeSplits.length; i++) {
					int nodePid = Integer.parseInt(nodeSplits[i]);

					Geometry gNode = wktReader.read(wktPoints.split(",")[i]);

					JSONObject aObject = new JSONObject();

					aObject.put("i", nodePid);
					
					aObject.put("g", Geojson.lonlat2Pixel(gNode.getCoordinate().x, gNode.getCoordinate().y, z, px, py));
					
					aObject.put("b", Integer.parseInt(isMains.split(",")[i]));
					
					maArray.add(aObject);
				}
				
				jsonM.put("a", maArray);
				
				snapshot.setG(new JSONArray());

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

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		RdCrossSearch s = new RdCrossSearch(conn);

		IObj obj = s.searchDataByPid(3313);

		System.out.println(obj.Serialize(null));
	}
}