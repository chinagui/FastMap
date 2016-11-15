package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdSameLinkSearch implements ISearch {

	private Connection conn;

	public RdSameLinkSearch(Connection conn) {

		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		return (IObj) new AbstractSelector(RdSameLink.class, conn).loadById(
				pid, false);
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID, GEOMETRY FROM ZONE_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'MASK=ANYINTERACT') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT LINK_PID, GEOMETRY FROM AD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2, 8307), 'MASK=ANYINTERACT') = 'TRUE' AND U_RECORD != 2), TMP3 AS (SELECT LINK_PID, GEOMETRY FROM LU_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:3, 8307), 'MASK=ANYINTERACT') = 'TRUE' AND U_RECORD != 2), TMP4 AS (SELECT /*+ INDEX(A) */ A.GROUP_ID, B.GEOMETRY FROM RD_SAMELINK_PART A, TMP1 B WHERE (A.TABLE_NAME = 'ZONE_LINK' AND A.LINK_PID = B.LINK_PID) AND U_RECORD != 2), TMP5 AS (SELECT /*+ INDEX(A) */ A.GROUP_ID, B.GEOMETRY FROM RD_SAMELINK_PART A, TMP2 B WHERE (A.TABLE_NAME = 'AD_LINK' AND A.LINK_PID = B.LINK_PID) AND U_RECORD != 2), TMP6 AS (SELECT /*+ INDEX(A) */ A.GROUP_ID, B.GEOMETRY FROM RD_SAMELINK_PART A, TMP3 B WHERE (A.TABLE_NAME = 'LU_LINK' AND A.LINK_PID = B.LINK_PID) AND U_RECORD != 2) SELECT * FROM TMP4 UNION ALL SELECT * FROM TMP5 UNION ALL SELECT * FROM TMP6 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			pstmt.setString(2, wkt);

			pstmt.setString(3, wkt);

			System.out.println(wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			List<Integer> groupIdList = new ArrayList<>();

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				int groupId = resultSet.getInt("group_id");

				if (!groupIdList.contains(groupId)) {

					groupIdList.add(groupId);

					snapshot.setI(String.valueOf(groupId));

					snapshot.setT(38);

					STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");

					JSONObject geojson = Geojson.spatial2Geojson(struct);

					Geojson.coord2Pixel(geojson, z, px, py);

					snapshot.setG(geojson.getJSONArray("coordinates"));

					list.add(snapshot);
				}
			}
		} catch (Exception e) {

			throw new SQLException(e);

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return list;
	}

}
