package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

public class IxPoiSearch implements ISearch {

	private Connection conn;

	public IxPoiSearch(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

		IObj ixPoi = (IObj) ixPoiSelector.loadById(pid, false);

		return ixPoi;
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

		//String sql = "with tmp1 as  (select pid, kind_code,x_guide, y_guide, geometry, row_id     from ix_poi    where sdo_relate(geometry, sdo_geometry(    :1    , 8307), 'mask=anyinteract') =          'TRUE'      and u_record != 2),  tmp2 as  (SELECT COUNT(1) PARENTCOUNT     FROM IX_POI_PARENT P, tmp1 a    WHERE p.parent_poi_pid = a.PID      and p.u_record != 2),  tmp3 as  (SELECT COUNT(1) CHILDCOUNT     FROM IX_POI_CHILDREN P, tmp1 a    WHERE p.child_poi_pid = a.PID      and p.u_record != 2) select  a.*,  b.status,  c.PARENTCOUNT,  d.CHILDCOUNT,  (SELECT NAME     FROM ix_poi_name p    WHERE p.POI_PID = a.PID      AND p.LANG_CODE = 'CHI'      AND p.NAME_CLASS = 1      AND p.NAME_TYPE = 2      AND p.u_record != 2) NAME   from tmp1 a, poi_edit_status b, tmp2 c, tmp3 d  where a.row_id = b.row_id";
		
		StringBuilder sb=new StringBuilder();
		
		sb.append("WITH TMP1 AS  (SELECT PID, KIND_CODE, X_GUIDE, Y_GUIDE, GEOMETRY, ROW_ID     FROM IX_POI    WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'MASK=ANYINTERACT') =          'TRUE'      AND U_RECORD != 2), TMP2 AS  (SELECT DISTINCT PARENT_POI_PID     FROM IX_POI_PARENT P, TMP1 A    WHERE P.PARENT_POI_PID = A.PID      AND P.U_RECORD != 2), TMP3 AS  (SELECT DISTINCT CHILD_POI_PID     FROM IX_POI_CHILDREN C, TMP1 A    WHERE C.CHILD_POI_PID = A.PID      AND C.U_RECORD != 2), TMP4 AS  (SELECT PN.NAME, PN.POI_PID     FROM IX_POI_NAME PN, TMP1 A    WHERE PN.POI_PID = A.PID      AND PN.LANG_CODE = 'CHI'      AND PN.NAME_CLASS = 1      AND PN.NAME_TYPE = 2      AND PN.U_RECORD != 2) SELECT            A.*,        B.STATUS,        (SELECT           COUNT(1)           FROM IX_POI_PARENT P          WHERE P.PARENT_POI_PID = A.PID AND P.U_RECORD!=2) PARENTCOUNT,        (SELECT            COUNT(1)           FROM IX_POI_CHILDREN C          WHERE C.CHILD_POI_PID = A.PID AND C.U_RECORD!=2) CHILDCOUNT,        P4.NAME   FROM TMP1 A, POI_EDIT_STATUS B, TMP4 P4  WHERE A.ROW_ID = B.ROW_ID AND P4.POI_PID=A.PID");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			//pstmt = conn.prepareStatement(sql);

			//System.out.println(sql);
			
			pstmt = conn.prepareStatement(sb.toString());

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				int parentCount = resultSet.getInt("parentCount");

				int childCount = resultSet.getInt("childCount");

				String haveParentOrChild = GetParentOrChild(parentCount,
						childCount);
				int status = resultSet.getInt("status");

				JSONObject m = new JSONObject();

				m.put("a", haveParentOrChild);
				m.put("b", status);
				m.put("d", resultSet.getString("kind_code"));

				m.put("e", resultSet.getString("name"));

				Double xGuide = resultSet.getDouble("x_guide");

				Double yGuide = resultSet.getDouble("y_guide");

				Geometry guidePoint = GeoTranslator.point2Jts(xGuide, yGuide);

				JSONObject guidejson = GeoTranslator.jts2Geojson(guidePoint);

				Geojson.point2Pixel(guidejson, z, px, py);

				m.put("c", guidejson.getJSONArray("coordinates"));

				snapshot.setM(m);

				snapshot.setT(21);

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

	private String GetParentOrChild(int parentCount, int childCount) {
		String haveParentOrChild = "0";

		if (parentCount > 0 && childCount > 0) {
			haveParentOrChild = "3";
		} else if (parentCount > 0) {
			haveParentOrChild = "1";
		} else if (childCount > 0) {
			haveParentOrChild = "2";
		}

		return haveParentOrChild;
	}

	public static void main(String[] args) throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);
		new IxPoiSearch(conn).searchDataByTileWithGap(215890, 99229, 18, 80);
	}
}
