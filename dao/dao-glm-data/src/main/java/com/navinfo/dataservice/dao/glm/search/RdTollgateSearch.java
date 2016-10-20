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
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * 
 * @Title: RdTollgateSearch.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午3:43:35
 * @version: v1.0
 */
public class RdTollgateSearch implements ISearch {

	private Connection conn;

	public RdTollgateSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdTollgateSelector selector = new RdTollgateSelector(this.conn);
		IObj obj = (IObj) selector.loadByIdOrderBySeqnum(pid, true);
		return obj;
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

		String sql = "WITH tmp1 AS ( SELECT a.geometry, a.node_pid FROM rd_node a, rd_tollgate b WHERE sdo_relate(a.geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' AND a.NODE_PID = b.NODE_PID and a.u_record != 2) select a.pid, tmp1.geometry as geometry  from rd_tollgate a, tmp1 WHERE a.node_pid = tmp1.node_pid AND a.u_record != 2";

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

				snapshot.setT(42);

				snapshot.setI(resultSet.getString("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

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
