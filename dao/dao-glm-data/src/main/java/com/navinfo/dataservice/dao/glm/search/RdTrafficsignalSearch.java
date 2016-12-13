/**
 * 
 */
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
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
* @ClassName: RdTrafficsignalSearch 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:45:46 
* @Description: TODO
*/
public class RdTrafficsignalSearch implements ISearch {
	
	private Connection conn;

	public RdTrafficsignalSearch(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdTrafficsignalSelector rdTrafficsignalSelector = new RdTrafficsignalSelector(conn);
		
		return (IObj) rdTrafficsignalSelector.loadById(pid, false);
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
		
		String sql = "WITH tmp1 AS (	SELECT a.geometry,a.node_pid FROM rd_node a,rd_cross_node b WHERE sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' AND a.NODE_PID = b.NODE_PID and a.u_record != 2) select a.pid,tmp1.geometry as geometry  from RD_TRAFFICSIGNAL a,tmp1 WHERE a.node_pid = tmp1.node_pid AND a.u_record != 2";
		
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

				snapshot.setT(27);

				snapshot.setI(resultSet.getInt("pid"));

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
