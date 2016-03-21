package com.navinfo.navicommons.geo.wkt;

import oracle.spatial.geometry.JGeometry;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 2010-8-5
 * Time: 14:09:47
 * To change this template use File | Settings | File Templates.
 */
public interface WKT {
     public String transform(JGeometry geometry);
}
