package com.navinfo.dataservice.engine.edit.comm.util.operate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdGscOperateUtils {
	public static JSONArray calCoordinateByNotSelfInter(JSONObject geojson, Geometry gscGeo) throws Exception {
		// 立交点的坐标
		double lon = gscGeo.getCoordinate().x;

		double lat = gscGeo.getCoordinate().y;

		JSONArray jaLink = geojson.getJSONArray("coordinates");

		JSONArray ja1 = new JSONArray();

		boolean hasFound = false;

		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);

			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
			if (!hasFound) {
				// 交点和形状点重合
				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
					hasFound = true;
					if (i == jaLink.size() - 2) {
						ja1.add(jaPE);
					}
				}
				// 交点在线段上
				else if (GeoTranslator.isIntersection(new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { lon, lat })) {
					ja1.add(jaPS);

					ja1.add(new double[] { lon, lat });
					hasFound = true;
					if (i == jaLink.size() - 2) {
						ja1.add(jaPE);
					}
				} else {
					if (i > 0) {
						ja1.add(jaPS);
					}
				}

			} else {
				ja1.add(jaPS);
				if (i == jaLink.size() - 2) {
					ja1.add(jaPE);
				}
			}
		}

		return ja1;
	}

	public static JSONArray calCoordinateBySelfInter(JSONObject geojson, Geometry gscGeo) throws Exception {

		// 立交点的坐标
		double lon = gscGeo.getCoordinate().x;

		double lat = gscGeo.getCoordinate().y;

		JSONArray jaLink = geojson.getJSONArray("coordinates");

		JSONArray ja1 = new JSONArray();

		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);

			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			// 判断点是否在线段上
			boolean isIntersection = GeoTranslator.isIntersectionInLine(
					new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
					new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { lon, lat });

			// 交点和形状点重合
			if (lon == jaPS.getDouble(0) && lat == jaPS.getDouble(1)) {
				ja1.add(jaPS);
				if (i == jaLink.size() - 2) {
					ja1.add(jaPE);
				}
				continue;
			} else if (isIntersection) {
				ja1.add(jaPS);
				// 交点在线段上
				ja1.add(new double[] { lon, lat });
				if (i == jaLink.size() - 2) {
					ja1.add(jaPE);
				}
			} else {
				if (i > 0) {
					ja1.add(jaPS);
				}
			}
		}

		return ja1;
	}

	/**
	 * 计算点在link上的形状点序号
	 * 
	 * @param gscGeo
	 *            点
	 * @param linkCoors
	 *            lin的形状点数组
	 * @return int类型序号
	 */
	public static List<Integer> calcShpSeqNum(Geometry gscGeo, Coordinate[] linkCoors) {

		List<Integer> shpSeqNum = new ArrayList<>();

		Coordinate gscCoor = gscGeo.getCoordinate();

		for (int i = 0; i < linkCoors.length; i++) {
			Coordinate linkCoor = linkCoors[i];

			if (gscCoor.x == linkCoor.x && gscCoor.y == linkCoor.y) {
				shpSeqNum.add(i);
			}
		}

		return shpSeqNum;
	}

	/**
	 * 检查是否是自相交
	 * 
	 * @param gscLinkList
	 *            立交组成线
	 * @return
	 */
	public static boolean checkIsSelfInter(List<IRow> gscLinkList) {
		boolean flag = false;

		if (gscLinkList.size() == 2) {
			RdGscLink link1 = (RdGscLink) gscLinkList.get(0);

			RdGscLink link2 = (RdGscLink) gscLinkList.get(1);

			if (link1.getLinkPid() == link2.getLinkPid()) {
				flag = true;
			}
		}

		return flag;
	}

	/**
	 * 检查是否自相交立交
	 * 
	 * @param linkMap
	 * @return
	 */
	public static boolean checkIsSelfGsc(Map<Integer, RdGscLink> linkMap) {
		boolean isSelfGsc = false;

		if (linkMap.size() >= 2) {
			Iterator<RdGscLink> iterator = linkMap.values().iterator();

			RdGscLink gscLink1 = iterator.next();

			RdGscLink gscLink2 = iterator.next();

			if (gscLink1.getLinkPid() == gscLink2.getLinkPid()) {
				isSelfGsc = true;
			}
		}
		return isSelfGsc;
	}

	/**
	 * 更新新建的立交对组成线上对已有的立交的影响
	 * 
	 * @param flag
	 * @param gsc
	 * @param linkCoor
	 * @param result
	 * @throws Exception
	 */
	public static void handleInterEffect(boolean flag, RdGsc gsc, Coordinate[] linkCoor, Result result)
			throws Exception {
		for (IRow gscLink : gsc.getLinks()) {

			RdGscLink link = (RdGscLink) gscLink;

			List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gsc.getGeometry(), linkCoor);

			JSONObject updateContent = new JSONObject();

			if (flag) {
				// 自相交立交组成线
				updateContent.put("shpSeqNum", shpSeqNumList.get(link.getZlevel()));
			} else {
				updateContent.put("shpSeqNum", shpSeqNumList.get(0));
			}

			boolean changed = link.fillChangeFields(updateContent);
			if (changed) {
				result.insertObject(link, ObjStatus.UPDATE, gsc.getPid());
			}
		}
	}

	/**
	 * 计算打断点在立交组成link上的位置
	 * 
	 * @param geojson
	 *            组成link的几何
	 * @param gscGeo
	 *            立交几何
	 * @param breakPoint
	 *            打断点几何
	 * @return 打断点在立交组成link上的位置
	 * @throws Exception
	 */
	public static int calCoordinateBySelfInter(JSONObject geojson, Geometry gscGeo, Geometry breakPoint)
			throws Exception {

		int result = -1;

		// 立交点的坐标
		double lon = gscGeo.getCoordinate().x;

		double lat = gscGeo.getCoordinate().y;

		double breakLon = breakPoint.getCoordinate().x;

		double breakLat = breakPoint.getCoordinate().y;

		JSONArray jaLink = geojson.getJSONArray("coordinates");

		boolean hasFisrtFound = false;

		boolean hasSecondFound = false;

		for (int i = 0; i < jaLink.size() - 1; i++) {

			JSONArray jaPS = jaLink.getJSONArray(i);

			if (jaPS.getDouble(0) == lon && jaPS.getDouble(1) == lat) {
				if (hasFisrtFound) {
					hasSecondFound = true;
				} else {
					hasFisrtFound = true;
				}
			}

			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			// 判断点是否在线段上
			boolean isIntersection = GeoTranslator.isIntersection(new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
					new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { breakLon, breakLat });

			if (isIntersection) {
				if (!hasFisrtFound) {
					result = 1;
				}
				if (hasFisrtFound && !hasSecondFound) {
					result = 2;
				}
				if (hasSecondFound) {
					result = 3;
				}
				return result;
			} else {
				// 打断点不在线上
			}
		}

		return result;
	}

	/**
	 * 根据link组成线（rdgsclink），返回lever和link对象(rdlink or rwlink or ...)
	 * 
	 * @param gscLinkMap
	 *            link level和rdgsclink的map
	 * @param linksGeometryList
	 *            link的几何集合
	 * @param conn
	 *            数据库链接
	 * @return lever和link map对象
	 * @throws Exception
	 */
	public static Map<Integer, IRow> handleLink(Map<Integer, RdGscLink> gscLinkMap, List<Geometry> linksGeometryList,
			Connection conn) throws Exception {

		Map<Integer, IRow> linkMap = new HashMap<>();

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		RwLinkSelector rwLinkSelector = new RwLinkSelector(conn);

		for (Entry<Integer, RdGscLink> rdGscLinkEntry : gscLinkMap.entrySet()) {

			int level = rdGscLinkEntry.getKey();

			RdGscLink rdGscLink = rdGscLinkEntry.getValue();

			int linkPid = rdGscLink.getLinkPid();

			String type = rdGscLink.getTableName();

			switch (type) {
			case "RDLINK":

				RdLink link = (RdLink) linkSelector.loadById(linkPid, true);

				Geometry geometry = link.getGeometry();

				geometry.setUserData(linkPid);

				linksGeometryList.add(geometry);

				rdGscLink.setTableName("RD_LINK");

				linkMap.put(level, link);
				break;
			case "RWLINK":

				RwLink rwLink = (RwLink) rwLinkSelector.loadById(linkPid, true);

				Geometry rwGeo = rwLink.getGeometry();

				rwGeo.setUserData(linkPid);

				linksGeometryList.add(rwGeo);

				rdGscLink.setTableName("RW_LINK");

				linkMap.put(level, rwLink);
				break;

			default:
				break;
			}
		}

		return linkMap;
	}

	public static RdGsc addRdGsc(Geometry gscGeo) throws Exception {
		RdGsc rdGsc = new RdGsc();

		rdGsc.setPid(PidService.getInstance().applyRdGscPid());

		rdGsc.setGeometry(gscGeo);

		// 处理标识默认为不处理
		rdGsc.setProcessFlag(1);

		return rdGsc;
	}

	/**
	 * 更新link几何信息
	 * 
	 * @param linkObj
	 *            link对象
	 * @param gscGeo
	 *            立交交点
	 * @param result
	 * @return 新的link的形状点
	 * @throws Exception
	 */
	public static JSONObject updateLinkGeo(RdGscLink rdGscLink, Geometry linkGeo, Geometry gscGeo) throws Exception {

		// link的几何
		JSONObject geojson = GeoTranslator.jts2Geojson(linkGeo);

		JSONArray ja1 = null;

		ja1 = RdGscOperateUtils.calCoordinateByNotSelfInter(geojson, gscGeo);

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject updateContent = new JSONObject();

		// 新的link的几何
		JSONObject geoJson = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geojson1), 0.00001, 5);

		updateContent.put("geometry", geoJson);

		Coordinate[] linkCoor = GeoTranslator.geojson2Jts(geoJson, 100000, 0).getCoordinates();

		// 获取link起终点标识
		int startEndFlag = GeometryUtils.getStartOrEndType(linkCoor, gscGeo);

		rdGscLink.setStartEnd(startEndFlag);

		// 计算形状点号：SHP_SEQ_NUM
		if (startEndFlag == 1) {
			rdGscLink.setShpSeqNum(0);
		} else if (startEndFlag == 2) {
			rdGscLink.setShpSeqNum(linkCoor.length - 1);
		} else {
			List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkCoor);
			rdGscLink.setShpSeqNum(shpSeqNumList.get(0));
		}
		return geoJson;
	}

	/**
	 * 更新自相交link几何信息
	 * 
	 * @param linkObj
	 *            link对象
	 * @param gscGeo
	 *            立交交点
	 * @param result
	 * @return 新的link的形状点
	 * @throws Exception
	 */
	public static JSONObject updateLinkGeoBySelf(RdGscLink rdGscLink, Geometry linkGeo, Geometry gscGeo)
			throws Exception {
		// link的几何
		JSONObject geojson = GeoTranslator.jts2Geojson(linkGeo);

		JSONArray ja1 = RdGscOperateUtils.calCoordinateBySelfInter(geojson, gscGeo);

		JSONObject geojson1 = new JSONObject();

		geojson1.put("type", "LineString");

		geojson1.put("coordinates", ja1);

		JSONObject updateContent = new JSONObject();

		// 新的link的几何
		JSONObject geoJson = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geojson1), 0.00001, 5);

		updateContent.put("geometry", geoJson);

		return geoJson;
	}

	/**
	 * 计算立交点在组成线上的形状点号
	 * 
	 * @param rdGscLink
	 * @param linkGeo
	 * @param gscGeo
	 * @param linkCoor
	 * @throws Exception
	 */
	public static void calShpSeqNum(RdGscLink rdGscLink, Geometry gscGeo, Coordinate[] linkCoor)
			throws Exception {
		List<Integer> shpSeqNumList = null;
		
		// 获取link起终点标识
		int startEndFlag = GeometryUtils.getStartOrEndType(linkCoor, gscGeo);

		rdGscLink.setStartEnd(startEndFlag);

		// 计算形状点号：SHP_SEQ_NUM
		if (startEndFlag == 1) {
			rdGscLink.setShpSeqNum(0);
		} else if (startEndFlag == 2) {
			rdGscLink.setShpSeqNum(linkCoor.length - 1);
		} else {
			shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkCoor);
			if (shpSeqNumList != null && shpSeqNumList.size()>1) {
				rdGscLink.setShpSeqNum(shpSeqNumList.get(rdGscLink.getZlevel()));
			} else {
				rdGscLink.setShpSeqNum(shpSeqNumList.get(0));
			}
		}
	}

	/**
	 * 检查线上是否已经在已知点位存在立交点
	 * @param gscGeo
	 * @param linkPidList
	 * @param conn
	 * @return boolean
	 * @throws Exception
	 */
	public static boolean checkIsHasGsc(Geometry gscGeo, List<Integer> linkPidList, Connection conn) throws Exception {
		boolean flag = false;
		
		RdGscSelector selector = new RdGscSelector(conn);
		
		List<RdGsc> rdGscList = selector.loadRdGscByInterLinkPids(linkPidList, false);

		for (RdGsc gsc : rdGscList) {
			if (gsc.getGeometry().equals(gscGeo)) {
				flag = true;
				break;
			}
		}
		return flag;
	}
}
