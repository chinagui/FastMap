package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @Title: RdSpeedbumpSearch.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月5日 下午4:25:37
 * @version: v1.0
 */
public class RdSpeedbumpSearch implements ISearch {

	private Connection conn;

	public RdSpeedbumpSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSpeedbumpSelector selector = new RdSpeedbumpSelector(this.conn);
		return (IObj) selector.loadById(pid, false);
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
		String sql = "with tmp1 as (select node_pid, geometry from rd_node where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' and u_record != 2) select a.bump_pid pid, b.geometry point_geom from rd_speedbump a, tmp1 b where a.node_pid = b.node_pid and u_record != 2";
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
				snapshot.setT(36);
				snapshot.setI(resultSet.getString("pid"));
				STRUCT struct = (STRUCT) resultSet.getObject("point_geom");
				JSONObject geojson = Geojson.spatial2Geojson(struct);
				Geojson.point2Pixel(geojson, z, px, py);
				snapshot.setG(geojson.getJSONArray("coordinates"));
				list.add(snapshot);
			}
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return list;
	}

}
