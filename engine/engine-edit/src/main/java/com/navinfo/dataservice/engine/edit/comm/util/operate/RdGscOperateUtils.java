package com.navinfo.dataservice.engine.edit.comm.util.operate;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
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
			
			//判断点是否在线段上
			boolean isIntersection = GeoTranslator.isIntersectionInLine(new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
					new double[] { jaPE.getDouble(0), jaPE.getDouble(1) }, new double[] { lon, lat });
			
			// 交点和形状点重合
			if (lon == jaPS.getDouble(0) && lat == jaPS.getDouble(1)) {
				ja1.add(jaPS);
				if (i == jaLink.size() - 2) {
					ja1.add(jaPE);
				}
				continue;
			}
			else if (isIntersection) {
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

			RdGscLink link2 = (RdGscLink) gscLinkList.get(0);

			if (link1.getLinkPid() == link2.getLinkPid()) {
				flag = true;
			}
		}

		return flag;
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
	public static void handleInterEffect(boolean flag, RdGsc gsc, Coordinate[] linkCoor, Result result) throws Exception {
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
}
