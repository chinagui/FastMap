/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
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

		String sql = "WITH TMP1           AS (	SELECT LINK_PID, GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1 , 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) , tmp2 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.LINK_PID AS objPid, sdo_util.to_wktgeometry_varchar(A.GEOMETRY) AS geometry,2 AS objType,c.ROAD_PID as childpid FROM TMP1 A, RD_ROAD_LINK B,Rd_object_road C WHERE A.LINK_PID = B.LINK_PID AND C.ROAD_PID = b.pid AND B.U_RECORD != 2 ), tmp3                     AS (SELECT /*+ leading(A,B) use_hash(A,B)*/ b.PID, A.LINK_PID AS objPid,sdo_util. to_wktgeometry_varchar(A .GEOMETRY) AS geometry,0 AS objType ,a.link_pid as childpid FROM TMP1 A, Rd_object_link b WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 ), TMP4                                  AS (	SELECT node_pid , GEOMETRY FROM RD_NODE WHERE SDO_RELATE ( GEOMETRY , SDO_GEOMETRY (:2 , 8307), 'mask=anyinteract' ) = 'TRUE' AND U_RECORD != 2) , tmp5 AS (	SELECT /*+ leading(A,B) use_hash(A,B)*/ C.PID, A.NODE_PID AS objPid,sdo_util.to_wktgeometry_varchar(A.GEOMETRY) AS geometry,1 AS objType, c.INTER_PID as childpid FROM TMP4 A, RD_INTER_NODE B,Rd_object_INTER C WHERE A.NODE_PID = B.NODE_PID AND C.inter_PID = b.pid AND B.U_RECORD != 2 ) SELECT tmp7.*,sdo_util.to_wktgeometry_varchar(tmp8.geometry) AS objGeo FROM(	SELECT tmp6.* FROM(	SELECT * FROM tmp2 UNION ALL 	SELECT * FROM tmp3 UNION ALL 	SELECT * FROM tmp5 ) tmp6 group by tmp6.pid,tmp6.objpid,tmp6.geometry,tmp6.objType,tmp6.childpid) tmp7 LEFT JOIN rd_object tmp8 ON tmp7.pid = tmp8.pid where tmp8.u_record !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		WKTReader wktReader = new WKTReader();

		try {
			pstmt = conn.prepareStatement(sql);

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			System.out.println(wkt);

			pstmt.setString(1, wkt);

			pstmt.setString(2, wkt);

			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			Map<Integer, Map<String, List<JSONObject>>> values = new HashMap<Integer, Map<String, List<JSONObject>>>();

			while (resultSet.next()) {

				List<Integer> linkPidList = new ArrayList<>();

				List<Integer> nodePidList = new ArrayList<>();

				int pid = resultSet.getInt("pid");

				// 处理主表pid，将主表pid加入Map
				if (!values.containsKey(pid)) {
					Map<String, List<JSONObject>> resultMap = new HashMap<String, List<JSONObject>>();

					List<JSONObject> objGeoList = new ArrayList<>();

					String objPoint = resultSet.getString("objGeo");

					Geometry objNode = wktReader.read(objPoint);

					JSONObject rdObjJSON = new JSONObject();

					rdObjJSON.put("objGeo",
							Geojson.lonlat2Pixel(objNode.getCoordinate().x, objNode.getCoordinate().y, z, px, py));

					objGeoList.add(rdObjJSON);

					resultMap.put("objGeo", objGeoList);

					values.put(pid, resultMap);
				}

				Map<String, List<JSONObject>> resultMap = values.get(pid);

				// type:0 表示返回的结果是rd_object_link的数据，type:1表示inter,type:2表示road
				int type = resultSet.getInt("objType");

				if (type != 1) {
					int linkPid = resultSet.getInt("objPid");

					if (!linkPidList.contains(linkPid)) {
						if (!resultMap.containsKey("link")) {
							resultMap.put("link", new ArrayList<JSONObject>());
						}

						List<JSONObject> linkList = resultMap.get("link");

						JSONObject linkJSON = new JSONObject();

						String linkWkt = resultSet.getString("geometry");

						int childPid = resultSet.getInt("childPid");

						JSONObject geojson = Geojson.wkt2Geojson(linkWkt);

						linkJSON.put("linkCor", geojson.getJSONArray("coordinates"));

						JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

						linkJSON.put("g", jo.getJSONArray("coordinates"));

						linkJSON.put("i", linkPid);

						// 要素类型
						linkJSON.put("t", type);

						// link所属的CRF交叉点或者road的pid
						linkJSON.put("p", childPid);

						linkList.add(linkJSON);

						resultMap.put("link", linkList);

						linkPidList.add(linkPid);
					}
				} else {
					String nodeWkt = resultSet.getString("geometry");

					int nodePid = resultSet.getInt("objpid");
					
					int childPid = resultSet.getInt("childPid");

					if (nodeWkt != null && !nodePidList.contains(nodePid)) {

						if (!resultMap.containsKey("node")) {
							resultMap.put("node", new ArrayList<JSONObject>());
						}

						List<JSONObject> nodeList = resultMap.get("node");

						Geometry nodeGeo = wktReader.read(nodeWkt);

						JSONObject nodeJSON = new JSONObject();

						nodeJSON.put("nodeCor", GeoTranslator.jts2Geojson(nodeGeo).getJSONArray("coordinates"));

						nodeJSON.put("g",
								Geojson.lonlat2Pixel(nodeGeo.getCoordinate().x, nodeGeo.getCoordinate().y, z, px, py));

						nodeJSON.put("i", nodePid);
						
						nodeJSON.put("t", 1);
						
						nodeJSON.put("p", childPid);
						
						nodeList.add(nodeJSON);

						resultMap.put("node", nodeList);

						nodePidList.add(nodePid);
					}
				}
			}

			for (Map.Entry<Integer, Map<String, List<JSONObject>>> entry : values.entrySet()) {
				int pid = entry.getKey();

				Map<String, List<JSONObject>> rdOjbMap = entry.getValue();

				SearchSnapshot snapshot = new SearchSnapshot();

				snapshot.setI(String.valueOf(pid));

				snapshot.setT(41);

				List<JSONObject> gObjList = rdOjbMap.get("objGeo");

				snapshot.setG(gObjList.get(0).getJSONArray("objGeo"));

				JSONArray gLinkArray = new JSONArray();

				List<JSONObject> linkObjList = rdOjbMap.get("link");

				for (JSONObject linkJObj : linkObjList) {
					gLinkArray.add(linkJObj);
				}

				List<JSONObject> nodeObjList = rdOjbMap.get("node");

				JSONArray gNodeArray = new JSONArray();

				if (CollectionUtils.isNotEmpty(nodeObjList)) {
					for (JSONObject nodeJObj : nodeObjList) {
						gNodeArray.add(nodeJObj);
					}
				}

				JSONObject jsonM = new JSONObject();

				Coordinate[] coordinates = getLineFromMuitPoint(gLinkArray, gNodeArray);

				Geometry metry = JGeometryUtil.getPolygonFromPoint(coordinates);
				
				Geometry boundary = metry.getBoundary();
				
				JSONObject obj = Geojson.link2Pixel(GeoTranslator.jts2Geojson(boundary), px, py, z);
				
				jsonM.put("a", gLinkArray);

				jsonM.put("b", gNodeArray);
				
				jsonM.put("c", obj.getJSONArray("coordinates"));

				snapshot.setM(jsonM);

				list.add(snapshot);

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

			JSONArray pointArray = obj.getJSONArray("linkCor");

			for (int j = 0; j < pointArray.size(); j++) {
				JSONArray point = pointArray.getJSONArray(j);

				double x = point.getDouble(0);

				double y = point.getDouble(1);

				if (!pointStr.contains(x + "_" + y)) {
					pointStr.add(x + "_" + y);
				}
			}
			obj.remove("linkCor");
		}

		for (int i = 0; i < nodeArray.size(); i++) {
			JSONObject obj = nodeArray.getJSONObject(i);

			JSONArray pointArray = obj.getJSONArray("nodeCor");

			double x = pointArray.getDouble(0);

			double y = pointArray.getDouble(1);

			if (!pointStr.contains(x + "_" + y)) {
				pointStr.add(x + "_" + y);
			}
			
			obj.remove("nodeCor");
		}

		Coordinate[] coordinates = new Coordinate[pointStr.size()];

		for (int i = 0; i < pointStr.size(); i++) {
			String[] point = pointStr.get(i).split("_");

			coordinates[i] = new Coordinate(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
		}

		return coordinates;
	}
}
