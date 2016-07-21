package com.navinfo.dataservice.engine.edit.bo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public abstract class LinkBo extends AbstractBo {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	protected Geometry geometry;
	
	public BreakResult breakoff(Point point) throws Exception {
		BreakResult result = new BreakResult();
		
		log.info("判断打断点是否在形状点上还是在线段上");
		Geometry geo = GeoTranslator.transform(point, 100000, 5);
		double lon = geo.getCoordinate().x;
		double lat = geo.getCoordinate().y;
		JSONArray leftLink = new JSONArray();
		JSONArray rightLink = new JSONArray();
		boolean hasFound = false;
		
		JSONObject geojson = GeoTranslator.jts2Geojson(geometry);
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				leftLink.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);

			if (!hasFound) {
				// 打断点在形状点上
				if (lon == jaPE.getDouble(0) && lat == jaPE.getDouble(1)) {
					leftLink.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					leftLink.add(new double[] { lon, lat });
					rightLink.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					if (i > 0) {
						leftLink.add(jaPS);
					}

					leftLink.add(jaPE);
				}
			} else {
				rightLink.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				rightLink.add(jaPE);
			}

		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}
		log.info("打断点在LINK上");
		
		JSONObject sGeojson = new JSONObject();
		sGeojson.put("type", "LineString");
		sGeojson.put("coordinates", leftLink);
		JSONObject eGeojson = new JSONObject();
		eGeojson.put("type", "LineString");
		eGeojson.put("coordinates", rightLink);
		
		result.setNewLeftGeometry(GeoTranslator.geojson2Jts(sGeojson,0.00001,5));
		result.setNewRightGeometry(GeoTranslator.geojson2Jts(eGeojson,0.00001,5));
		return result;
	}

	@Override
	public void setPo(IObj po) {}

}
