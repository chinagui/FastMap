package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class IxPointaddressSearch implements ISearch {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	
	public IxPointaddressSearch(Connection conn) {
		super();
		this.conn = conn;
	}
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<? extends IRow> searchDataByPids(List<Integer> pidList) throws Exception {
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
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap, JSONArray noQFilter) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		StringBuilder sb = new StringBuilder();

		sb.append("WITH TMP1 AS (SELECT PID, IDCODE, X_GUIDE, Y_GUIDE, GEOMETRY, GUIDE_LINK_PID, ROW_ID,DPR_NAME,DP_NAME "
				+ "FROM IX_POINTADDRESS WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'MASK=ANYINTERACT') "
				+ "= 'TRUE' AND U_RECORD != 2) "
				+ "SELECT TMP.*  FROM (");

		if (noQFilter != null) {
			if (noQFilter.size() > 0) {
				sb.append("SELECT A.*, B.STATUS,B.QUICK_TASK_ID,"
						+ "B.MEDIUM_TASK_ID  FROM TMP1 A,DAY_EDIT_STATUS B "
						+ " WHERE A.PID = B.PID AND B.ELEMENT=1 AND B.QUICK_TASK_ID = 0 AND B.STATUS <> 0 ");
				if (noQFilter.contains(1) && noQFilter.size() == 1) {
					sb.append(" AND B.MEDIUM_TASK_ID <> 0 ");

				}
				if (noQFilter.contains(2) && noQFilter.size() == 1) {
					sb.append(" AND B.MEDIUM_TASK_ID = 0 ");

				}
			} else {
				return null;
			}
		} else {
			sb.append("SELECT A.*, 0 STATUS,0 QUICK_TASK_ID ,"
					+ "0 MEDIUM_TASK_ID  FROM TMP1 A");
		}
		sb.append(" ) TMP ");
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			log.info(sb.toString());
			pstmt = conn.prepareStatement(sb.toString());

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				int status = resultSet.getInt("status");

				JSONObject m = new JSONObject();

				m.put("b", status);
				m.put("d", resultSet.getString("DPR_NAME"));

				m.put("e", resultSet.getString("DP_NAME"));

				m.put("g", resultSet.getInt("ROW_ID") == 0 ? 0 : 1);
				m.put("quickFlag", resultSet.getInt("quick_task_id") == 0 ? 0
						: 1);
				m.put("mediumFlag", resultSet.getInt("medium_task_id") == 0 ? 0
						: 1);
				m.put("n", resultSet.getString("IDCODE") == null ? ""
						: resultSet.getString("IDCODE"));

				// Double xGuide = resultSet.getDouble("x_guide");
				//
				// Double yGuide = resultSet.getDouble("y_guide");
				//
				// Geometry guidePoint = GeoTranslator.point2Jts(xGuide,
				// yGuide);
				//
				// JSONObject guidejson = GeoTranslator.jts2Geojson(guidePoint);
				//
				// Geojson.point2Pixel(guidejson, z, px, py);
				//
				// m.put("c", guidejson.getJSONArray("coordinates"));

				m.put("c", resultSet.getDouble("x_guide"));
				m.put("f", resultSet.getDouble("y_guide"));
				m.put("l", resultSet.getInt("GUIDE_LINK_PID"));

				snapshot.setM(m);

				snapshot.setT(55);

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
