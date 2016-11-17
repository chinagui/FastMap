package com.navinfo.dataservice.engine.edit.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/***
 * 
 * @author zhaokk 要素制作功能操作类
 * 
 */
public class BasicServiceUtils {
	/***
	 * 
	 * @param catchLinks
	 *            排序json数组
	 * @param key
	 *            排序的key
	 * @return
	 */
	public static JSONArray breakSortParas(JSONArray catchLinks,
			final String key) {
		List<JSONObject> jsonValues = new ArrayList<JSONObject>();
		JSONArray sortedJsonArray = new JSONArray();
		for (int i = 0; i < catchLinks.size(); i++) {
			jsonValues.add(catchLinks.getJSONObject(i));
		}
		Collections.sort(jsonValues, new Comparator<JSONObject>() {

			@Override
			public int compare(JSONObject a, JSONObject b) {
				Integer valA = 0;
				Integer valB = 0;
				try {
					valA = (Integer) a.get(key);
					valB = (Integer) b.get(key);
				} catch (JSONException e) {
				}

				return valA.compareTo(valB);

			}
		});
		for (int i = 0; i < catchLinks.size(); i++) {
			sortedJsonArray.add(jsonValues.get(i));
		}
		return sortedJsonArray;

	}

	/***
	 * 组件连续打断参数 按照相同linkPid进行分组
	 * 
	 * @param catchLinks
	 * @return
	 */
	public static JSONArray groupBreaksPara(JSONArray catchLinks) {
		// 参数排序
		JSONArray sortArry = breakSortParas(catchLinks, "linkPid");
		JSONArray jsonResult = new JSONArray();
		JSONObject objResult = new JSONObject();
		JSONArray breakNode = new JSONArray();
		for (int i = 0; i < sortArry.size(); i++) {
			JSONObject obj = sortArry.getJSONObject(i);
			JSONObject nodeObj = new JSONObject();
			if (i == 0) {
				objResult.put("linkPid", obj.getInt("linkPid"));
				nodeObj.put("lon", obj.getDouble("lon"));
				nodeObj.put("lat", obj.getDouble("lat"));
				nodeObj.put("breakNode", obj.getInt("breakNode"));
				breakNode.add(nodeObj);

			} else {
				if (obj.getInt("linkPid") == objResult.getInt("linkPid")) {
					nodeObj.put("breakNode", obj.getInt("breakNode"));
					nodeObj.put("lon", obj.getDouble("lon"));
					nodeObj.put("lat", obj.getDouble("lat"));
					breakNode.add(nodeObj);

				} else {

					objResult.put("breakNodePids", breakNode);
					jsonResult.add(objResult);
					objResult = new JSONObject();
					breakNode = new JSONArray();
					objResult.put("linkPid", obj.getInt("linkPid"));
					nodeObj.put("breakNode", obj.getInt("breakNode"));
					nodeObj.put("lon", obj.getDouble("lon"));
					nodeObj.put("lat", obj.getDouble("lat"));
					breakNode.add(nodeObj);

				}
			}
			if (i == sortArry.size() - 1) {
				objResult.put("breakNodePids", breakNode);
				jsonResult.add(objResult);
			}
		}
		return jsonResult;
	}

	/***
	 * 组装连续打断的参数
	 * 
	 * @param obj
	 *            原有打断参数
	 * @param dbId
	 *            大区库id
	 * @return
	 */
	public static JSONObject getBreaksPara(JSONObject obj, int dbId) {

		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", obj.get("linkPid"));
		breakJson.put("dbId", dbId);
		JSONObject data = new JSONObject();
		if (obj.getJSONArray("breakNodePids").size() <= 1) {
			JSONObject jsonNode = obj.getJSONArray("breakNodePids")
					.getJSONObject(0);
			data.put("breakNodePid", jsonNode.getInt("breakNode"));
			data.put("longitude", jsonNode.get("lon"));
			data.put("latitude", jsonNode.get("lat"));

		} else {
			JSONArray array = new JSONArray();
			for (int j = 0; j < obj.getJSONArray("breakNodePids").size(); j++) {
				JSONObject jsonBreak = obj.getJSONArray("breakNodePids")
						.getJSONObject(j);
				JSONObject breakObj = new JSONObject();
				breakObj.put("breakNodePid", jsonBreak.getInt("breakNode"));
				breakObj.put("longitude", jsonBreak.get("lon"));
				breakObj.put("latitude", jsonBreak.get("lat"));
				array.add(breakObj);

			}
			data.put("breakNodes", array);
		}

		breakJson.put("data", data);
		return breakJson;

	}

	/***
	 * 打断参数重组，只是重组有打断点的参数
	 * 
	 * @param array
	 * @return
	 */
	public static JSONArray getBreakArray(JSONArray array) {
		JSONArray breakArray = new JSONArray();
		for (int i = 0; i < array.size(); i++) {
			if (array.getJSONObject(i).containsKey("breakNode")) {
				breakArray.add(array.getJSONObject(i));
			}
		}
		if (breakArray.size() > 0) {
			breakArray = BasicServiceUtils.groupBreaksPara(breakArray);
		}
		return breakArray;
	}

	/***
	 * 创建多次打断方法 有序的分割参数集合
	 * 
	 * @param points
	 * @return
	 */
	public static JSONArray getSplitOrderPara(List<Point> points,
			JSONArray breakNodes) {
		JSONArray breakArr = new JSONArray();
		for (Point point : points) {
			for (int i = 0; i < breakNodes.size(); i++) {
				JSONObject obj = breakNodes.getJSONObject(i);
				double lon = obj.getDouble("longitude");
				double lat = obj.getDouble("latitude");
				if (point.getX() == lon && point.getY() == lat) {
					// 匹配打断参数，加以区分
					breakArr.add(obj);
					break;
				}
			}
		}
		// 组件分割参数用以区分
		for (Object obj : breakArr) {
			JSONObject jsonObj = (JSONObject) obj;
			double lon = jsonObj.getDouble("longitude");
			double lat = jsonObj.getDouble("latitude");
			int nodePid = jsonObj.getInt("breakNodePid");
			jsonObj.put("lon", lon);
			jsonObj.put("lat", lat);
			jsonObj.put("nodePid", nodePid);
			jsonObj.remove("longitude");
			jsonObj.remove("latitude");
			jsonObj.remove("breakNodePid");

		}
		return breakArr;
	}

	/***
	 * 一个点打断link 打断判断打断的点是否在形状点上，或者是否在线段上，切割线
	 * @param  geometry 要打断线的集合
	 * @param  point  被那个点打断
	 * result 返回生成两条线的点几何
	 * @throws Exception
	 */
	public static List<JSONArray> breakpoint(Geometry geometry, Point point)
			throws Exception {

		JSONObject geojson = GeoTranslator.jts2Geojson(geometry);
		List<JSONArray> arrays = new ArrayList<JSONArray>();
		double lon = point.getCoordinate().x * 100000;
		double lat = point.getCoordinate().y * 100000;
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		boolean hasFound = false;// 打断的点是否和形状点重合或者是否在线段上
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
			if (!hasFound) {
				// 打断点和形状点重合
				if (Math.abs(lon - jaPE.getDouble(0)) < 0.0000001
						&& Math.abs(lat - jaPE.getDouble(1)) < 0.0000001) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					ja1.add(jaPE);
				}
			} else {
				ja2.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				ja2.add(jaPE);
			}
		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}
		// 生成新的link
		arrays.add(ja1);
		arrays.add(ja2);
		return arrays;
	}

}
