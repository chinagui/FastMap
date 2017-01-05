package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

/**
 * @ClassName: TipsGeoUtils.java
 * @author y
 * @date 2017-1-3 下午7:46:12
 * @Description: TODO
 * 
 */
public class TipsGeoUtils {

	/***
	 * 线跨图幅打断
	 * 
	 * @param map
	 * @param result
	 * @throws Exception
	 */
	public static List<Geometry> cutGeoByMeshes(Geometry geo)
			throws Exception {

		List<Geometry> resultGeosList = new ArrayList<Geometry>();
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		// 不跨图幅
		if (meshes.size() == 1) {
			return null;
		}
		// 跨图幅
		else {
			Iterator<String> it = meshes.iterator();
			while (it.hasNext()) {
				String meshIdStr = it.next();
				Geometry geomInter = MeshUtils.linkInterMeshPolygon(geo,
						GeoTranslator.transform(MeshUtils.mesh2Jts(meshIdStr),
								1, 5));
				if (geomInter instanceof GeometryCollection) {
					int geoNum = geomInter.getNumGeometries();
					for (int i = 0; i < geoNum; i++) {
						Geometry subGeo = geomInter.getGeometryN(i);
						if (subGeo instanceof LineString) {
							subGeo = GeoTranslator.geojson2Jts(
									GeoTranslator.jts2Geojson(subGeo), 1, 5);

							resultGeosList.addAll(getCutLinksWithMesh(subGeo));
						}
					}
				} else {
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);
					resultGeosList.addAll(getCutLinksWithMesh(geomInter));
				}
			}
		}
		return resultGeosList;
	}

	/***
	 * 跨图幅打断后的线是有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 
	 * @param g
	 * @param maps
	 * @throws Exception
	 */
	private static List<Geometry> getCutLinksWithMesh(Geometry g) throws Exception {
		List<Geometry> geos = new ArrayList<Geometry>();
		if (g != null) {

			if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
				geos.add(g);
			}
			if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
				for (int i = 0; i < g.getNumGeometries(); i++) {
					geos.add(g.getGeometryN(i));
				}
			}
		}
		return geos;
		
		
	}

}
