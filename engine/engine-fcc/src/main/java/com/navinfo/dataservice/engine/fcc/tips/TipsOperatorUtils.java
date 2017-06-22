package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

/**
 * @ClassName: TipsOperatorUtils.java
 * @author y
 * @date 2017-6-21 下午8:20:42
 * @Description: TODO
 * 
 */
public class TipsOperatorUtils {

	/***
	 * 将一条几何，跨图幅打断为多条
	 * 
	 * @param map
	 * @param result
	 * @throws Exception
	 */
	public static List<Geometry>  cutGeoByMeshes(Geometry geo) throws Exception {

		List<Geometry> resultGeoList = new ArrayList<Geometry>();

		//计算图幅个数
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		// 不跨图幅
		if (meshes.size() == 1) {
			
			return null;
		}
		// 跨图幅
		else {
			//分别按照个图幅打断
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
							
							resultGeoList.add(subGeo);
						}
					}
				} else {
					geomInter = GeoTranslator.geojson2Jts(
							GeoTranslator.jts2Geojson(geomInter), 1, 5);

					resultGeoList.add(geomInter);
				}
			}
		}
		
		return resultGeoList;

	}

}
