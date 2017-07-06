package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import net.sf.json.JSONArray;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

import net.sf.json.JSONObject;

public class RdSlopeSearch implements ISearch {

	private Connection conn;

	public RdSlopeSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdSlopeSelector selector = new RdSlopeSelector(conn);

		IObj obj = (IObj) selector.loadById(pid, false);
		return obj;
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {
		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<IRow> rows = selector.loadByIds(pidList, false, true);

		return rows;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<>();

		String sql = "WITH TMP1 AS (SELECT A.GEOMETRY, A.LINK_PID, A.S_NODE_PID FROM RD_LINK A WHERE sdo_within_distance(A.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE'AND A.U_RECORD != 2) SELECT A.PID, A.TYPE, A.LINK_PID, A.NODE_PID, TMP1.GEOMETRY, TMP1.S_NODE_PID FROM RD_SLOPE A, TMP1 WHERE A.LINK_PID = TMP1.LINK_PID AND A.U_RECORD != 2";

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

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("type"));

				snapshot.setM(m);

				snapshot.setT(24);

				snapshot.setI(resultSet.getInt("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

                Geometry geometry = GeoTranslator.geojson2Jts(geojson);

                int nodePid = resultSet.getInt("node_pid");
                int sNodePid = resultSet.getInt("s_node_pid");
                if (nodePid != sNodePid) {
                    geometry = geometry.reverse();
                }

                double length = GeometryUtils.getLinkLength(geometry);

                if (30.0d < length) {
                    length = 30.0d;
                } else {
                    length = length / 3.0d;
                }

                Coordinate coordinate = GeometryUtils.getPointOnLineStringDistance((LineString) geometry, length);

				JSONArray array = Geojson.lonlat2Pixel(coordinate.x, coordinate.y, z, px, py);

				snapshot.setG(array);

				list.add(snapshot);
			}
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
		}

		return list;
	}

	public static void main(String[] args) throws Exception {

		Connection conn = DBConnector.getInstance().getConnectionById(11);

		RdSlopeSearch s = new RdSlopeSearch(conn);

		IObj obj = s.searchDataByPid(132837);

		System.out.println(obj.Serialize(null));
	}
}
