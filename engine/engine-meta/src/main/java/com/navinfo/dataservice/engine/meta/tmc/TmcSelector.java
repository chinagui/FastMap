/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: TmcSelector
 * @author Zhang Xiaolong
 * @date 2016年11月11日 下午1:32:07
 * @Description: TODO
 */
public class TmcSelector {

	private Connection conn;

	public TmcSelector() {
	}

	public TmcSelector(Connection conn) {
		this.conn = conn;
	}

	public List<SearchSnapshot> queryTmcPoint(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT TMC_ID, LOC_CODE,GEOMETRY FROM TMC_POINT WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) SELECT A.TMC_ID,A.LOC_CODE,A.GEOMETRY, B.TRANSLATE_NAME FROM TMP1 A LEFT JOIN TMC_POINT_TRANSLATENAME B ON A.TMC_ID = B.TMC_ID WHERE B.NAME_FLAG = 1";

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

				snapshot.setI(resultSet.getString("TMC_ID"));

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("LOC_CODE"));

				m.put("b", resultSet.getString("TRANSLATE_NAME"));

				snapshot.setM(m);

				snapshot.setT(48);

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return list;
	}
}
