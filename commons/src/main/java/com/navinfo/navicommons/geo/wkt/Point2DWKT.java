package com.navinfo.navicommons.geo.wkt;

import oracle.spatial.geometry.JGeometry;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 2010-8-5
 * Time: 14:11:51
 */
public class Point2DWKT implements WKT {
    /**
     * POINT (6588856.0 3957911.0)
     *
     * @param geometry
     * @return
     */
    public String transform(JGeometry geometry) {

        StringBuilder builder = new StringBuilder();
        builder.append("POINT (");
        double[] points = geometry.getOrdinatesArray();
        for (int i = 0; i < points.length; i++) {
            double point = points[i];
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
