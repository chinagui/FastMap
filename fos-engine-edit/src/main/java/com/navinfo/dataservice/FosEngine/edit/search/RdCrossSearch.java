package com.navinfo.dataservice.FosEngine.edit.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class RdCrossSearch implements ISearch {
	
	private static final WKTReader wktReader = new WKTReader();

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
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
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

		String sql = "with tmp1 as  (select node_pid     from rd_node    where sdo_relate(geometry,                     sdo_geometry(:1,                                  8307),                     'mask=anyinteract') = 'TRUE') select pid,        listagg(a.node_pid, ',') within group(order by a.node_pid) node_pids,        listagg(sdo_util.to_wktgeometry_varchar(b.geometry), ',') within group(order by a.node_pid) wkts   from rd_cross_node a, rd_node b  where exists (select null from tmp1 b where a.node_pid = b.node_pid)    and a.node_pid = b.node_pid  group by a.pid";

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
				
				JSONObject jsonM = new JSONObject();

				snapshot.setI(String.valueOf(resultSet.getInt("pid")));
				
				snapshot.setT(8);

				jsonM.put("a",resultSet.getString("node_pids"));

				String wktPoints = resultSet.getString("wkts");
				
				JSONArray gArray = new JSONArray();
				
				String[] splits = wktPoints.split(",");
				
				for(String w : splits){
					Geometry gNode = wktReader.read(w);
					
					gArray.add(Geojson.lonlat2Pixel(gNode.getCoordinate().x,gNode.getCoordinate().y,z,px,py));
				}

				snapshot.setG(gArray);
				
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
		ConfigLoader.initDBConn("C:/Users/wangshishuai3966/git/FosEngine/FosEngine/src/config.properties");
		
		Connection conn = DBOraclePoolManager.getConnection(11);
		
		RdCrossSearch s = new RdCrossSearch(conn);
		
		IObj obj = s.searchDataByPid(3313);
		
		System.out.println(obj.Serialize(null));
	}
}
