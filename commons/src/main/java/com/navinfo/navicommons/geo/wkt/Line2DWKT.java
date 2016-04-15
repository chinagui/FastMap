package com.navinfo.navicommons.geo.wkt;

import oracle.spatial.geometry.JGeometry;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 2010-8-5
 * Time: 14:11:51
 * To change this template use File | Settings | File Templates.
 */
public class Line2DWKT implements WKT {
    /**
     * LINESTRING (6572972.0 3950164.0, 6573008.0 3950166.0, 6573137.0 3950182.0, 6573204.0 3950189.0)
     *
     * @param geometry
     * @return
     */
    public String transform(JGeometry geometry) {

        StringBuilder builder = new StringBuilder();
        builder.append("LINESTRING (");
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
        builder.append(")");
        String wkt = builder.toString();

        return wkt;
    }
}
