/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.geo.computation.JGeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: RdObjectSearch
 * @author Zhang Xiaolong
 * @date 2016年8月12日 下午4:29:15
 * @Description: TODO
 */
public class RdObjectSearch implements ISearch {

	private Connection conn;

	public RdObjectSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return (IObj) new AbstractSelector(RdObject.class, conn).loadById(pid, false);
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

		String sql = " WITH TMP1    AS (	SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) , tmp2 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.LINK_PID,A.GEOMETRY FROM TMP1 A, RD_ROAD_LINK B,Rd_object_road C WHERE A.LINK_PID = B.LINK_PID AND C.ROAD_PID = b.pid AND B.U_RECORD != 2 ), tmp3  AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ b.PID, A.LINK_PID,A.GEOMETRY FROM TMP1 A, Rd_object_link b WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 ), TMP4  AS (	SELECT node_pid, GEOMETRY FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) , tmp5 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.NODE_PID,A.GEOMETRY FROM TMP4 A, RD_INTER_NODE B,Rd_object_INTER C WHERE A.NODE_PID = B.NODE_PID AND C.inter_PID = b.pid AND B.U_RECORD != 2 ) , tmp7 as( SELECT tmp6.PID,listagg(tmp6.LINK_PID, ',') within group( ORDER BY tmp6.LINK_PID) AS link_pids,listagg(sdo_util.to_wktgeometry_varchar( tmp6.GEOMETRY), ';') within group( ORDER BY tmp6.LINK_PID) AS link_wkts,listagg(tmp5.node_pid, ',') within group( ORDER BY tmp5.node_pid) AS node_pids,listagg(sdo_util.to_wktgeometry_varchar( tmp5. geometry), ',') within group( ORDER BY tmp5.node_pid) AS node_wkts FROM(	SELECT * FROM tmp2 UNION ALL 	SELECT * FROM tmp3) tmp6 LEFT JOIN tmp5 ON tmp6.pid = tmp5.pid GROUP BY tmp6.pid) select tmp7.*,tmp8.GEOMETRY from tmp7 left join rd_object tmp8 on tmp7.pid = tmp8.pid";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		WKTReader wktReader = new WKTReader();

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);

			pstmt.setString(2, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			List<Integer> pidList = new ArrayList<>();

			while (resultSet.next()) {

				int pid = resultSet.getInt("pid");

				if (!pidList.contains(pid)) {
					SearchSnapshot snapshot = new SearchSnapshot();

					snapshot.setI(String.valueOf(resultSet.getInt("pid")));

					snapshot.setT(40);

					STRUCT struct = (STRUCT) resultSet.getObject("geometry");

					JSONObject rdObjGeo = Geojson.spatial2Geojson(struct);

					Geojson.point2Pixel(rdObjGeo, z, px, py);

					snapshot.setG(rdObjGeo.getJSONArray("coordinates"));
					
					JSONObject jsonM = new JSONObject();
					
					String nodePids = resultSet.getString("node_pids");

					if (StringUtils.isNotEmpty(nodePids)) {
						Set<String> nodePidSet = new HashSet<>();

						String[] splits = nodePids.split(",");

						String wktPoints = resultSet.getString("node_wkts");

						JSONArray gArray = new JSONArray();

						String[] nodeWktSplits = wktPoints.split(",");

						for (int i = 0; i < splits.length; i++) {

							if (!nodePidSet.contains(splits[i])) {
								JSONObject gObject = new JSONObject();

								Geometry gNode = wktReader.read(nodeWktSplits[i]);

								gObject.put("g", Geojson.lonlat2Pixel(gNode.getCoordinate().x, gNode.getCoordinate().y,
										z, px, py));
								gObject.put("i", splits[i]);

								gArray.add(gObject);

								nodePidSet.add(splits[i]);
							}
						}

						jsonM.put("b", gArray);
					}

					String linkPids = resultSet.getString("link_pids");

					Set<String> linkSet = new HashSet<>();

					if (StringUtils.isNotEmpty(linkPids)) {

						String[] linkSplits = linkPids.split(",");

						String wktLinks = resultSet.getString("link_wkts");

						JSONArray gLinkArray = new JSONArray();

						String[] linkWktSplits = wktLinks.split(";");

						for (int i = 0; i < linkSplits.length; i++) {

							if (!linkSet.contains(linkSplits[i])) {
								JSONObject gObject = new JSONObject();

								JSONObject geojson = Geojson.wkt2Geojson(linkWktSplits[i]);
								
								JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

								gObject.put("g", jo.getJSONArray("coordinates"));
								gObject.put("i", linkSplits[i]);

								gLinkArray.add(gObject);

								linkSet.add(linkSplits[i]);
							}
						}

						jsonM.put("a", gLinkArray);

						Coordinate[] coordinates = getLineFromMuitPoint(gLinkArray,snapshot.getG());
						
						Geometry metry = JGeometryUtil.getPolygonFromPoint(coordinates);
						
						jsonM.put("c", GeoTranslator.jts2Geojson(metry).getJSONArray("coordinates"));
						
						snapshot.setM(jsonM);
					}

					list.add(snapshot);
				}
			}
		} catch (Exception e) {

			throw new SQLException(e);
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

	private Coordinate[] getLineFromMuitPoint(JSONArray linkArray, JSONArray nodeArray) {
		
		List<String> pointStr = new ArrayList<>();

		for (int i = 0; i < linkArray.size(); i++) {
			JSONObject obj = linkArray.getJSONObject(i);

			JSONArray pointArray = obj.getJSONArray("g");

			for (int j = 0; j < pointArray.size(); j++) {
				JSONArray point = pointArray.getJSONArray(j);

				int x = point.getInt(0);

				int y = point.getInt(1);

				if (!pointStr.contains(x + "_" + y)) {
					pointStr.add(x + "_" + y);
				}
			}
		}
		
		for(int i = 0;i<nodeArray.size();i++)
		{
			JSONObject obj = nodeArray.getJSONObject(i);

			JSONArray pointArray = obj.getJSONArray("g");
			
			int x = pointArray.getInt(0);

			int y = pointArray.getInt(1);

			if (!pointStr.contains(x + "_" + y)) {
				pointStr.add(x + "_" + y);
			}
		}
		
		Coordinate[] coordinates = new Coordinate[pointStr.size()];

		for (int i = 0; i < pointStr.size(); i++) {
			String[] point = pointStr.get(i).split("_");

			coordinates[i] = new Coordinate(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
		}
		
		return coordinates;
	}
}
