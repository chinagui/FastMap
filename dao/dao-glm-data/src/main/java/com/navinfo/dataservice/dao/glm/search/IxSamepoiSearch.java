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
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class IxSamepoiSearch implements ISearch {

	private Connection conn;

	public IxSamepoiSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		IxSamepoiSelector selector = new IxSamepoiSelector(conn);
		IObj ixSamepoi = (IObj) selector.loadById(pid, false);
		return ixSamepoi;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		// TODO Auto-generated method stub
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
		String sql = "with tmp1 as (select pid from ix_poi where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') = 'TRUE' and u_record != 2), select t1.geometry, t2.group_id from tmp1 t1, ix_samepoi t2, ix_samepoi_part t3 where t1.pid = t3.poi_id and t2.group_id = t3.group_id and t2.u_record != 2 and t3.u_record != 2";
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
				snapshot.setT(33);
				snapshot.setI(resultSet.getInt("group_id"));
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
