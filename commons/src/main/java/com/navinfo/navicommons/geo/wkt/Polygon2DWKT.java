package com.navinfo.navicommons.geo.wkt;

import oracle.spatial.geometry.JGeometry;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 2010-8-5
 * Time: 14:11:51
 * To change this template use File | Settings | File Templates.
 */
public class Polygon2DWKT implements WKT {
    /**
     * POLYGON ((6608177.0 3981810.0, 6608208.0 3981600.0, 6608492.0 3981592.0, 6608512.0 3981599.0, 6608525.0 3981711.0, 6608516.0 3981799.0, 6608340.0 3981816.0, 6608177.0 3981810.0, 6608177.0 3981810.0))
     *
     * @param geometry
     * @return
     */
    public String transform(JGeometry geometry) {

        StringBuilder builder = new StringBuilder();
        builder.append("POLYGON ((");
        double[] points = geometry.getOrdinatesArray();
        for (int i = 0; i < points.length; i++) {
            double point = points[i];
            if (i > 1 && i % 2 == 0) {
                builder.append(",");
            }
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(point);
        }
        builder.append("))");
        String wkt = builder.toString();

        return wkt;
    }
}
