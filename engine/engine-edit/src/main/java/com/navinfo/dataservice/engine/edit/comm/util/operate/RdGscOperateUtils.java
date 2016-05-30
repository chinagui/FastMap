package com.navinfo.dataservice.engine.edit.comm.util.operate;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
}
