/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.navicommons.geo.computation.JGeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
		RdObjectSelector objSelector = new RdObjectSelector(conn);
		return (IObj) objSelector.loadById(pid, false);
	}

	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
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

		String sql = "WITH TMP1 AS (SELECT LINK_PID FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT NODE_PID FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP1_1 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_ROAD C WHERE C.ROAD_PID IN (SELECT /*+index(B)*/ distinct B.PID FROM TMP1 A, RD_ROAD_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 AND B.PID = C.ROAD_PID) AND C.U_RECORD != 2 ), TMP1_2 AS (SELECT /*+leading(A,B)use_hash(A,B)*/ B.PID FROM TMP1 A, RD_OBJECT_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 ), TMP2_1 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_INTER C WHERE C.INTER_PID IN (SELECT /*+index(B)*/ distinct B.PID FROM TMP2 A, RD_INTER_NODE B WHERE A.NODE_PID = B.NODE_PID AND B.U_RECORD != 2 AND B.PID = C.INTER_PID) AND C.U_RECORD != 2), TMP1_3 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_INTER C WHERE C.INTER_PID in (SELECT /*+index(B)*/ distinct B.PID FROM TMP1 A, RD_INTER_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 AND B.PID = C.INTER_PID) AND C.U_RECORD != 2), TMP3 AS (SELECT PID FROM TMP1_1 UNION SELECT TMP1_2.PID FROM TMP1_2,tmp1_1 where tmp1_2.pid != TMP1_1.pid UNION SELECT TMP2_1.PID FROM TMP2_1,TMP1_2,tmp1_1 where TMP2_1.pid != TMP1_1.pid UNION SELECT PID FROM TMP1_3) SELECT TMP3.PID, SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(C.GEOMETRY) AS GEOMETRY FROM TMP3 LEFT JOIN RD_OBJECT C ON TMP3.PID = C.PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			//扩圈1000像素
			String wkt = MercatorProjection.getWktWithGap(x, y, z, 1000);

			pstmt.setString(1, wkt);

			pstmt.setString(2, wkt);

			resultSet = pstmt.executeQuery();

			System.out.println("2：" + DateUtils.dateToString(new Date()));

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				int pid = resultSet.getInt("pid");

				String linkWkt = resultSet.getString("geometry");

				JSONObject geojson = Geojson.wkt2Geojson(linkWkt);
				
				Geojson.coord2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				snapshot.setI(pid);
				
				snapshot.setT(41);
				//查询子表数据
				loadChildData(snapshot, px, py, z);
				
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

	private void loadChildData(SearchSnapshot snapshot, double px, double py,int z) throws Exception {

		int pid = snapshot.getI();

		String sql = "select tmp.*,SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(C.GEOMETRY) as geometry from( select 1 as type,B.LINK_PID as lnpid,A.INTER_PID as cpid from RD_OBJECT_INTER A,RD_INTER_LINK B where A.INTER_PID = b.PID and a.pid = :1 and a.U_RECORD !=2 and b.U_RECORD !=2 union all select 2 as type,B.LINK_PID as lnpid,A.road_PID as cpid from RD_OBJECT_ROAD A,RD_ROAD_LINK B where A.ROAD_PID = b.PID and a.pid = :2 and a.U_RECORD !=2 and b.U_RECORD !=2 union all select 0 as type,link_pid,pid as cpid from rd_object_link where pid = :3 and u_record !=2）tmp left join rd_link c on tmp.lnpid = c.link_pid union all select tmp.*,SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(C.GEOMETRY) as geometry from(select 3 as type,B.NODE_PID as lnpid,a.INTER_PID as cpid from RD_OBJECT_INTER A,RD_INTER_NODE B where A.INTER_PID = b.PID and a.pid = :4 and a.U_RECORD !=2 and b.U_RECORD !=2)tmp left join rd_node C on tmp.lnpid = c.node_pid";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			pstmt.setInt(2, pid);

			pstmt.setInt(3, pid);

			pstmt.setInt(4, pid);
			
			JSONObject jsonM = new JSONObject();

			resultSet = pstmt.executeQuery();
			
			JSONArray linkArray = new JSONArray();
			
			JSONArray nodeArray = new JSONArray();

			while (resultSet.next()) {

				int type = resultSet.getInt("type");

				JSONObject linkJObj = null;

				JSONObject nodeJObj = null;

				int lnPid = resultSet.getInt("lnPid");

				int cPid = resultSet.getInt("cPid");

				String lnGeometry = resultSet.getString("geometry");
				
				//转换为瓦片的几何
				JSONObject geojson = Geojson.wkt2Geojson(lnGeometry);
				
				if (type != 3) {
					//线几何对象
					linkJObj = new JSONObject();

					linkJObj.put("i", lnPid);

					linkJObj.put("p", cPid);

					linkJObj.put("linkCor", geojson.getJSONArray("coordinates"));
					
					Geojson.coord2Pixel(geojson, z, px, py);
					
					linkJObj.put("g", geojson.getJSONArray("coordinates"));
					
					linkJObj.put("t", type);
					
					linkArray.add(linkJObj);
				} else {
					//点几何对象
					nodeJObj = new JSONObject();

					nodeJObj.put("i", lnPid);

					nodeJObj.put("p", cPid);
					
					nodeJObj.put("t", 1);

					nodeJObj.put("nodeCor", geojson.getJSONArray("coordinates"));
					
					Geojson.coord2Pixel(geojson, z, px, py);
					
					nodeJObj.put("g", geojson.getJSONArray("coordinates"));
					
					nodeArray.add(nodeJObj);
				}
			}
			
			//包络线几何
			Coordinate[] cors = getLineFromMuitPoint(linkArray,nodeArray);
			
			Geometry metry = JGeometryUtil.getBuffer(cors);
			
			Geometry boundary = metry.getBoundary();
			
			boundary = GeoTranslator.transform(boundary,1,5);
			
			Coordinate[] cs = boundary.getCoordinates();
			
			double[][] ps = new double[cs.length][2];

			for (int i = 0; i < cs.length; i++) {
				ps[i][0] = cs[i].x;

				ps[i][1] = cs[i].y;
			}
			
			JSONObject geojson = new JSONObject();       

			geojson.put("type", "LineString");

			geojson.put("coordinates", ps);
			
			JSONObject obj = Geojson.link2Pixel(geojson, px, py, z);
			
			//CRF组成link对象数组
			jsonM.put("a", linkArray);
			
			//CRF组成node对象数组
			jsonM.put("b", nodeArray);
			
			jsonM.put("c", obj.getJSONArray("coordinates"));
			
			snapshot.setM(jsonM);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			DbUtils.close(pstmt);
			DbUtils.close(resultSet);
		}
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

		if(nodeArray != null)
		{
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
		}

		Coordinate[] coordinates = new Coordinate[pointStr.size()];

		for (int i = 0; i < pointStr.size(); i++) {
			String[] point = pointStr.get(i).split("_");

			coordinates[i] = new Coordinate(Double.parseDouble(point[0]), Double.parseDouble(point[1]));
		}

		return coordinates;
	}
}
