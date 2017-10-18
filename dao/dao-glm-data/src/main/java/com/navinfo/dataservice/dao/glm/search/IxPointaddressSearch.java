package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;

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
						+ "B.MEDIUM_TASK_ID  FROM TMP1 A,pointaddress_edit_status B "
						+ " WHERE A.PID = B.PID AND B.QUICK_TASK_ID = 0 AND B.STATUS <> 0 ");
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

	
	public JSONObject searchMainDataByPid(int pid) throws Exception {
		JSONObject json=new JSONObject();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT I.PID, I.IDCODE, I.X_GUIDE, I.Y_GUIDE, I.GEOMETRY, I.GUIDE_LINK_PID, I.ROW_ID,I.DPR_NAME,");
		sb.append("I.DP_NAME,I.MEMOIRE,I.MEMO,I.U_RECORD, ");
		sb.append("P.STATUS,P.FRESH_VERIFIED,P.RAW_FIELDS FROM IX_POINTADDRESS I,POINTADDRESS_EDIT_STATUS P WHERE I.PID=P.PID AND I.PID="+pid);
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		LogReader logRead = new LogReader(conn);
		try {
			log.info(sb.toString());
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				json.put("pid",resultSet.getInt("PID"));
				json.put("idcode",resultSet.getString("IDCODE"));
				json.put("xGuide",resultSet.getDouble("X_GUIDE"));
				json.put("yGuide",resultSet.getDouble("Y_GUIDE"));
			
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				JSONObject geojson = Geojson.spatial2Geojson(struct);
				json.put("geometry",geojson);
				json.put("guideLinkPid", resultSet.getInt("GUIDE_LINK_PID"));
				json.put("rowId", resultSet.getString("ROW_ID"));
				json.put("dprName", resultSet.getString("DPR_NAME"));

				json.put("dpName", resultSet.getString("DP_NAME"));
				json.put("memoire", resultSet.getString("MEMOIRE"));
				json.put("memo", resultSet.getString("MEMO"));
				json.put("uRecord", resultSet.getInt("U_RECORD"));
				json.put("verifiedFlag", resultSet.getInt("FRESH_VERIFIED"));
				json.put("status", resultSet.getInt("STATUS"));
				json.put("rawFields", resultSet.getInt("RAW_FIELDS"));
				json.put("geoLiveType",ObjType.IXPOINTADDRESS.toString());
				json.put("state",logRead.getObjectState(resultSet.getInt("PID"), "IX_POINTADDRESS"));
				
				
			}
			return json;
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
	}}
