/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.JGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
	private Logger logger = Logger.getLogger(RdObjectSearch.class);

	private int dbId;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public RdObjectSearch(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdObjectSelector objSelector = new RdObjectSelector(conn);
		return (IObj) objSelector.loadById(pid, false);
	}

	@Override
	public List<IRow> searchDataByPids(List<Integer> pidList) throws Exception {

		RdObjectSelector selector = new RdObjectSelector(conn);

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
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT NODE_PID FROM RD_NODE WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:2, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2), TMP1_1 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_ROAD C WHERE C.ROAD_PID IN (SELECT /*+index(B)*/ distinct B.PID FROM TMP1 A, RD_ROAD_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 AND B.PID = C.ROAD_PID) AND C.U_RECORD != 2 ), TMP1_2 AS (SELECT /*+leading(A,B)use_hash(A,B)*/ B.PID FROM TMP1 A, RD_OBJECT_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 ), TMP2_1 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_INTER C WHERE C.INTER_PID IN (SELECT /*+index(B)*/ distinct B.PID FROM TMP2 A, RD_INTER_NODE B WHERE A.NODE_PID = B.NODE_PID AND B.U_RECORD != 2 AND B.PID = C.INTER_PID) AND C.U_RECORD != 2), TMP1_3 AS (SELECT /*+index(C)*/ C.PID FROM RD_OBJECT_INTER C WHERE C.INTER_PID in (SELECT /*+index(B)*/ distinct B.PID FROM TMP1 A, RD_INTER_LINK B WHERE A.LINK_PID = B.LINK_PID AND B.U_RECORD != 2 AND B.PID = C.INTER_PID) AND C.U_RECORD != 2), TMP3 AS (SELECT distinct PID FROM TMP1_1 UNION SELECT distinct PID FROM TMP1_2 UNION SELECT distinct PID FROM TMP2_1 UNION SELECT distinct PID FROM TMP1_3) SELECT TMP3.PID, SDO_UTIL.TO_WKTGEOMETRY_VARCHAR(C.GEOMETRY) AS GEOMETRY FROM TMP3 LEFT JOIN RD_OBJECT C ON TMP3.PID = C.PID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			// 扩圈1000像素
			String wkt = MercatorProjection.getWktWithGap(x, y, z, 1000);

			System.out.println(wkt);

			pstmt.setString(1, wkt);

			pstmt.setString(2, wkt);

			resultSet = pstmt.executeQuery();

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
				// 查询子表数据
				loadChildData(snapshot, px, py, z, wkt);

				list.add(snapshot);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			throw e;
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

	private void loadChildData(SearchSnapshot snapshot, double px, double py,
			int z, String wkt) throws Exception {

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

				// 转换为瓦片的几何
				JSONObject geojson = Geojson.wkt2Geojson(lnGeometry);

				if (type != 3) {
					// 线几何对象
					linkJObj = new JSONObject();

					linkJObj.put("i", lnPid);

					linkJObj.put("p", cPid);

					linkJObj.put("linkCor", geojson.getJSONArray("coordinates"));

					Geojson.coord2Pixel(geojson, z, px, py);

					linkJObj.put("g", geojson.getJSONArray("coordinates"));

					linkJObj.put("t", type);

					linkArray.add(linkJObj);
				} else {
					// 点几何对象
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
			// 处理rdOBject 跨大区
			RdObject rdObject = (RdObject) this.searchDataByPid(pid);
			JSONArray rdInterLinks = new JSONArray();
			JSONArray rdRodLinks = new JSONArray();
			JSONArray rdObjectLinks = new JSONArray();

			for (int i = 0; i < linkArray.size(); i++) {
				JSONObject obj = linkArray.getJSONObject(i);
				if (obj.getInt("t") == 1) {
					rdInterLinks.add(obj);
				}
				if (obj.getInt("t") == 2) {
					rdRodLinks.add(obj);
				}
				if (obj.getInt("t") == 0) {
					rdObjectLinks.add(obj);
				}
			}
			// 判断RdObject是否跨大区
			boolean isCheckRdInterNodesArea = this.isCheckRdInterNodesArea(
					rdObject, nodeArray);
			boolean isCheckRdInterLinksArea = this.isCheckRdInterLinksArea(
					rdObject, rdInterLinks);
			boolean isCheckRdRoadLinksArea = this.isCheckRdRoadLinksArea(
					rdObject, rdRodLinks);
			boolean isCheckRdObjectLinksArea = this.isCheckRdObjectLinksArea(
					rdObject, rdObjectLinks);
			if (isCheckRdInterLinksArea || isCheckRdInterNodesArea
					|| isCheckRdRoadLinksArea || isCheckRdObjectLinksArea) {
				this.addRdObjectForArea(rdObject, rdInterLinks, rdRodLinks,
						rdObjectLinks, nodeArray, isCheckRdInterNodesArea,
						isCheckRdInterLinksArea, isCheckRdRoadLinksArea,
						isCheckRdObjectLinksArea, wkt, px, py, z);
				linkArray = new JSONArray();
				linkArray.addAll(rdInterLinks);
				linkArray.addAll(rdRodLinks);
				linkArray.addAll(rdObjectLinks);
			}

			// 包络线几何
			Coordinate[] cors = getLineFromMuitPoint(linkArray, nodeArray);

			Geometry metry = JGeometryUtil.getBuffer(cors);

			Geometry boundary = metry.getBoundary();

			boundary = GeoTranslator.transform(boundary, 1, 5);

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

			// CRF组成link对象数组
			jsonM.put("a", linkArray);

			// CRF组成node对象数组
			jsonM.put("b", nodeArray);

			jsonM.put("c", obj.getJSONArray("coordinates"));

			snapshot.setM(jsonM);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		} finally {
			DbUtils.close(pstmt);
			DbUtils.close(resultSet);
		}
	}

	private boolean isCheckRdInterNodesArea(RdObject rdObject,
			JSONArray rdInterNodes) {
		if (rdObject.getInters() != null && rdObject.getInters().size() > 0) {
			List<Integer> interNodes = new ArrayList<Integer>();
			for (IRow row : rdObject.getInters()) {
				RdObjectInter rdInter = (RdObjectInter) row;
				if (rdInter.getNodes() != null && rdInter.getNodes().size() > 0) {
					for (IRow InterNode : rdInter.getNodes()) {
						RdInterNode node = (RdInterNode) InterNode;
						interNodes.add(node.getNodePid());
					}
				}

			}
			if (rdInterNodes.size() != interNodes.size()) {
				return true;
			}

		}
		return false;

	}

	private boolean isCheckRdInterLinksArea(RdObject rdObject,
			JSONArray rdInterLinks) {
		if (rdObject.getInters() != null && rdObject.getInters().size() > 0) {
			List<Integer> interLinks = new ArrayList<Integer>();
			for (IRow row : rdObject.getInters()) {
				RdObjectInter rdInter = (RdObjectInter) row;
				if (rdInter.getLinks() != null && rdInter.getLinks().size() > 0) {
					for (IRow InterLink : rdInter.getLinks()) {
						RdInterLink link = (RdInterLink) InterLink;
						interLinks.add(link.getLinkPid());
					}
				}

			}
			if (rdInterLinks.size() != interLinks.size()) {
				return true;
			}

		}
		return false;

	}

	private boolean isCheckRdRoadLinksArea(RdObject rdObject,
			JSONArray rdRodLinks) {
		if (rdObject.getRoads() != null && rdObject.getRoads().size() > 0) {
			List<Integer> roadLinks = new ArrayList<Integer>();
			for (IRow row : rdObject.getRoads()) {
				RdObjectRoad rdRoad = (RdObjectRoad) row;
				if (rdRoad.getLinks() != null && rdRoad.getLinks().size() > 0) {
					for (IRow roadLink : rdRoad.getLinks()) {
						RdRoadLink link = (RdRoadLink) roadLink;
						roadLinks.add(link.getLinkPid());
					}
				}

			}
			if (roadLinks.size() != rdRodLinks.size()) {
				return true;
			}

		}
		return false;

	}

	private boolean isCheckRdObjectLinksArea(RdObject rdObject,
			JSONArray rdObjectLinks) {
		if (rdObject.getLinks() != null && rdObject.getLinks().size() > 0) {
			if (rdObject.getLinks().size() != rdObjectLinks.size()) {
				return true;
			}
		}
		return false;

	}

	/***
	 * @author zhaokk 处理rdobect 跨大区渲染
	 * @param rdObject
	 * @param rdInterLinks
	 * @param rdRodLinks
	 * @param rdObjectLinks
	 * @param nodeArray
	 * @param isCheckRdInterNodesArea
	 * @param isCheckRdInterLinksArea
	 * @param isCheckRdRoadLinksArea
	 * @param isCheckRdObjectLinksArea
	 * @param wkt
	 * @param px
	 * @param py
	 * @param z
	 * @throws Exception
	 */
	private void addRdObjectForArea(RdObject rdObject, JSONArray rdInterLinks,
			JSONArray rdRodLinks, JSONArray rdObjectLinks, JSONArray nodeArray,
			boolean isCheckRdInterNodesArea, boolean isCheckRdInterLinksArea,
			boolean isCheckRdRoadLinksArea, boolean isCheckRdObjectLinksArea,
			String wkt, double px, double py, int z) throws Exception {
		Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(wkt, 3);

		if (dbIds == null) {
			return;
		}
		if (dbIds.size() == 1 && dbIds.contains(this.getDbId())) {
			return;
		}
		Map<Integer, List<Integer>> mapNodes = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> mapInterLinks = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> mapRoadLinks = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> mapObjectLinks = new HashMap<Integer, List<Integer>>();
		// 计算跨大区rdinter的node
		if (isCheckRdInterNodesArea) {
			mapNodes = this.getAreaNodeMapforRdObject(rdObject, nodeArray);
		}
		// 计算跨大区rdinter的link
		if (isCheckRdInterLinksArea) {
			mapInterLinks = this.getAreaInterLinkMapforRdObject(rdObject,
					rdInterLinks);

		}
		// 计算跨大road的link
		if (isCheckRdRoadLinksArea) {
			mapRoadLinks = this.getAreaRdRoadMapforRdObject(rdObject,
					rdRodLinks);
		}
		// 计算跨大RdObjectLink的link
		if (isCheckRdObjectLinksArea) {
			mapObjectLinks = this.getAreaObjectLinkMapforRdObject(rdObject,
					rdObjectLinks);
		}
		List<Integer> areaNodes = this.getAreaPids(mapNodes);
		List<Integer> areaInterLinks = this.getAreaPids(mapInterLinks);
		List<Integer> areaRoadLinks = this.getAreaPids(mapRoadLinks);
		List<Integer> areaObjectLinks = this.getAreaPids(mapObjectLinks);
		for (int dbId : dbIds) {
			Connection connection = null;
			try {
				logger.info("dbId========" + dbId);
				if (this.getDbId() == dbId) {
					continue;
				}
				connection = DBConnector.getInstance().getConnectionById(dbId);

				if (areaNodes.size() > 0) {

					this.addAreaNodeForRdObject(connection, areaNodes,
							mapNodes, nodeArray, px, py, z);

				}
				if (areaInterLinks.size() > 0) {

					this.addAreaLinkForRdObject(connection, rdObject,
							areaInterLinks, mapInterLinks, rdInterLinks, 1, px,
							py, z);
				}

				if (areaRoadLinks.size() > 0) {
					this.addAreaLinkForRdObject(connection, rdObject,
							areaRoadLinks, mapRoadLinks, rdRodLinks, 1, px, py,
							z);
				}

				if (areaObjectLinks.size() > 0) {

					this.addAreaLinkForRdObject(connection, rdObject,
							areaObjectLinks, mapObjectLinks, rdObjectLinks, 0,
							px, py, z);

				}
				if (!this.isCheckRdInterLinksArea(rdObject, rdInterLinks)
						&& !this.isCheckRdInterNodesArea(rdObject, nodeArray)
						&& !this.isCheckRdRoadLinksArea(rdObject, rdRodLinks)
						&& !this.isCheckRdObjectLinksArea(rdObject,
								rdObjectLinks)) {
					break;
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e;

			} finally {
				DBUtils.closeConnection(connection);
			}

		}

	}

	/***
	 * 补充接边link
	 * 
	 * @param conn
	 * @param rdObject
	 * @param areaLinks
	 * @param mapLinks
	 * @param arrayLinks
	 * @param flag
	 * @param px
	 * @param py
	 * @param z
	 * @throws Exception
	 */
	private void addAreaLinkForRdObject(Connection conn, RdObject rdObject,
			List<Integer> areaLinks, Map<Integer, List<Integer>> mapLinks,
			JSONArray arrayLinks, int flag, double px, double py, int z)
			throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(conn);
		List<IRow> rows = linkSelector.loadByIds(areaLinks, false, false);
		if (rows.size() > 0) {
			for (IRow row : rows) {
				// 线几何对象
				RdLink link = (RdLink) row;
				JSONObject geojson = GeoTranslator.jts2Geojson(
						link.getGeometry(), 0.00001, 5);
				JSONObject linkJObj = new JSONObject();

				linkJObj.put("i", link.getPid());

				linkJObj.put("linkCor", geojson.getJSONArray("coordinates"));

				Geojson.coord2Pixel(geojson, z, px, py);

				linkJObj.put("g", geojson.getJSONArray("coordinates"));
				if (flag == 0) {
					linkJObj.put("t", 1);
					linkJObj.put("p", rdObject.getPid());
				}
				if (flag == 1) {
					linkJObj.put("t", 2);
					for (int cPid : mapLinks.keySet()) {
						List<Integer> links = mapLinks.get(cPid);
						if (links.contains(link.getPid())) {
							linkJObj.put("p", cPid);
							break;
						}
					}

				}

				arrayLinks.add(linkJObj);
			}
		}

	}

	/**
	 * 补充接边的node
	 * 
	 * @param conn
	 * @param areaNodes
	 * @param mapNodes
	 * @param nodeArray
	 * @param px
	 * @param py
	 * @param z
	 * @throws Exception
	 */
	private void addAreaNodeForRdObject(Connection conn,
			List<Integer> areaNodes, Map<Integer, List<Integer>> mapNodes,
			JSONArray nodeArray, double px, double py, int z) throws Exception {
		RdNodeSelector nodeSelector = new RdNodeSelector(conn);
		List<IRow> rows = nodeSelector.loadByIds(areaNodes, false, false);
		if (rows.size() > 0) {
			for (IRow row : rows) {

				RdNode node = (RdNode) row;
				JSONObject geojson = GeoTranslator.jts2Geojson(
						node.getGeometry(), 0.00001, 5);

				JSONObject nodeJObj = new JSONObject();
				nodeJObj.put("i", node.getPid());

				for (int cPid : mapNodes.keySet()) {
					List<Integer> nodes = mapNodes.get(cPid);
					if (nodes.contains(node.getPid())) {
						nodeJObj.put("p", cPid);
						break;
					}
				}

				nodeJObj.put("t", 1);

				nodeJObj.put("nodeCor", geojson.getJSONArray("coordinates"));

				Geojson.coord2Pixel(geojson, z, px, py);

				nodeJObj.put("g", geojson.getJSONArray("coordinates"));

				nodeArray.add(nodeJObj);
			}
		}

	}

	/**
	 * 计算接边的pid
	 * 
	 * @author zhaokk
	 * @param map
	 * @return
	 */
	private List<Integer> getAreaPids(Map<Integer, List<Integer>> map) {
		List<Integer> areaPids = new ArrayList<Integer>();
		for (List<Integer> maps : map.values()) {
			areaPids.addAll(maps);
		}
		return areaPids;

	}

	/***
	 * 提取接边对应inter 的node
	 * 
	 * @author zhaokk
	 * @param rdObject
	 * @param nodeArray
	 * @return
	 */
	private Map<Integer, List<Integer>> getAreaNodeMapforRdObject(
			RdObject rdObject, JSONArray nodeArray) {
		Map<Integer, List<Integer>> mapNodes = new HashMap<Integer, List<Integer>>();

		for (IRow row : rdObject.getInters()) {

			RdObjectInter inter = (RdObjectInter) row;
			List<Integer> interNodes = new ArrayList<Integer>();
			List<Integer> interNodesArea = new ArrayList<Integer>();
			for (IRow iRow : inter.getNodes()) {
				RdInterNode interNode = (RdInterNode) iRow;
				interNodes.add(interNode.getNodePid());

			}

			for (int i = 0; i < nodeArray.size(); i++) {
				JSONObject object = nodeArray.getJSONObject(i);
				if (object.getInt("p") == inter.getPid()) {
					interNodesArea.add(object.getInt("i"));
				}
			}

			interNodes.removeAll(interNodesArea);
			if (interNodes.size() > 0) {
				mapNodes.put(inter.getPid(), interNodes);
			}

		}
		return mapNodes;
	}

	/***
	 * @author zhaokk 提取接边对应road 的link
	 * @param rdObject
	 * @param rdRodLinks
	 * @return
	 */
	private Map<Integer, List<Integer>> getAreaRdRoadMapforRdObject(
			RdObject rdObject, JSONArray rdRodLinks) {
		Map<Integer, List<Integer>> mapRoadLinks = new HashMap<Integer, List<Integer>>();

		for (IRow row : rdObject.getRoads()) {
			RdObjectRoad rdRoad = (RdObjectRoad) row;
			List<Integer> roadLinks = new ArrayList<Integer>();
			List<Integer> roadLinksArea = new ArrayList<Integer>();
			for (IRow iRow : rdRoad.getLinks()) {
				RdRoadLink roadLink = (RdRoadLink) iRow;
				roadLinks.add(roadLink.getLinkPid());

			}

			for (int i = 0; i < rdRodLinks.size(); i++) {
				JSONObject object = rdRodLinks.getJSONObject(i);
				if (object.getInt("p") == rdRoad.getPid()) {
					roadLinksArea.add(object.getInt("i"));
				}
			}

			roadLinks.removeAll(roadLinksArea);
			if (roadLinks.size() > 0) {
				mapRoadLinks.put(rdRoad.getPid(), roadLinks);
			}

		}
		return mapRoadLinks;

	}

	/***
	 * @author zhaok 提取接边对应object 的link
	 * @param rdObject
	 * @param rdObjectLinks
	 * @return
	 */
	private Map<Integer, List<Integer>> getAreaObjectLinkMapforRdObject(
			RdObject rdObject, JSONArray rdObjectLinks) {
		Map<Integer, List<Integer>> mapObjectLinks = new HashMap<Integer, List<Integer>>();

		List<Integer> objectLinks = new ArrayList<Integer>();
		List<Integer> objectLinksArea = new ArrayList<Integer>();
		for (IRow row : rdObject.getLinks()) {
			RdObjectLink rdObjectLink = (RdObjectLink) row;

			objectLinks.add(rdObjectLink.getLinkPid());
		}
		for (int i = 0; i < rdObjectLinks.size(); i++) {
			JSONObject object = rdObjectLinks.getJSONObject(i);
			objectLinksArea.add(object.getInt("i"));
		}
		objectLinks.removeAll(objectLinksArea);
		if (objectLinks.size() > 0) {
			mapObjectLinks.put(rdObject.getPid(), objectLinks);
		}
		return mapObjectLinks;
	}

	/**
	 * @author zhaokk 提取接边对应inter 的link
	 * @param rdObject
	 * @param rdInterLinks
	 * @return
	 */
	private Map<Integer, List<Integer>> getAreaInterLinkMapforRdObject(
			RdObject rdObject, JSONArray rdInterLinks) {

		Map<Integer, List<Integer>> mapInterLinks = new HashMap<Integer, List<Integer>>();
		for (IRow row : rdObject.getInters()) {
			RdObjectInter inter = (RdObjectInter) row;
			List<Integer> interLinks = new ArrayList<Integer>();
			List<Integer> interLinksArea = new ArrayList<Integer>();
			for (IRow iRow : inter.getLinks()) {
				RdInterLink interLink = (RdInterLink) iRow;
				interLinks.add(interLink.getLinkPid());

			}

			for (int i = 0; i < rdInterLinks.size(); i++) {
				JSONObject object = rdInterLinks.getJSONObject(i);
				if (object.getInt("p") == inter.getPid()) {
					interLinksArea.add(object.getInt("i"));
				}
			}

			interLinks.removeAll(interLinksArea);
			if (interLinks.size() > 0) {
				mapInterLinks.put(inter.getPid(), interLinks);
			}

		}
		return mapInterLinks;
	}

	private Coordinate[] getLineFromMuitPoint(JSONArray linkArray,
			JSONArray nodeArray) {

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

		if (nodeArray != null) {
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

			coordinates[i] = new Coordinate(Double.parseDouble(point[0]),
					Double.parseDouble(point[1]));
		}

		return coordinates;
	}

}
