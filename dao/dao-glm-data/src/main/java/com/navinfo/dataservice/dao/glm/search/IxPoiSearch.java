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

		String sql = "select pid,  kind_code,      x_guide,        y_guide,        geometry,        (SELECT PS.STATUS FROM POI_EDIT_STATUS PS WHERE I.ROW_ID = PS.ROW_ID) STATUS,        (SELECT COUNT(1)           FROM IX_POI_PARENT P          WHERE p.parent_poi_pid = I.PID) PARENTCOUNT,        (SELECT COUNT(1)           FROM IX_POI_CHILDREN P          WHERE p.child_poi_pid = I.PID) CHILDCOUNT,        (SELECT NAME           FROM ix_poi_name          WHERE POI_PID = I.PID            AND LANG_CODE = 'CHI'            AND NAME_CLASS = 1            AND NAME_TYPE = 2) NAME   from ix_poi i  where sdo_relate(geometry, sdo_geometry(:1, 8307), 'mask=anyinteract') =        'TRUE'    and u_record != 2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			System.out.println(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			System.out.println(wkt);

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
