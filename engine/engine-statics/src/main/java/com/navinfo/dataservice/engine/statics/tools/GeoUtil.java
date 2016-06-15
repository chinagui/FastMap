package com.navinfo.dataservice.engine.statics.tools;

import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class GeoUtil {

	public static String getGridFromPoint(Geometry point) {
		Coordinate[] coordinate = point.getCoordinates();
		CompGridUtil gridUtil = new CompGridUtil();
		String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
		return grid;
	}
}
