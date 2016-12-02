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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "with tmp1 as  (select node_pid     from rd_node    where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2) select  /*+ index(b) */ pid,        listagg(a.node_pid, ',') within group(order by a.node_pid) node_pids,        listagg(sdo_util.to_wktgeometry_varchar(b.geometry), ',') within group(order by a.node_pid) wkts,        listagg(a.is_main, ',') within group(order by a.node_pid) is_mains   from rd_cross_node a, rd_node b  where exists (select null from tmp1 c where a.node_pid = c.node_pid)    and a.node_pid = b.node_pid    and a.u_record != 2    and b.u_record != 2  group by a.pid";

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

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));

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
					
					aObject.put("b", isMains.split(",")[i]);
					
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