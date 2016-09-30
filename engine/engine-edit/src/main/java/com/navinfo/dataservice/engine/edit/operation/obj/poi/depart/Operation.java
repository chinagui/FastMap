package com.navinfo.dataservice.engine.edit.operation.obj.poi.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于维护节点分离对POI对象的影响
 * Created by chaixin on 2016/9/19 0019.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载IxPoiSelector
        IxPoiSelector poiSelector = new IxPoiSelector(this.conn);
        // 获取该link挂接的所有POI
        List<IxPoi> pois = poiSelector.loadIxPoiByLinkPid(oldLink.pid(), true);
        // 当分离后的线没有跨越图幅时
        if (newLinks.size() == 1) {
            Geometry linkGeo = newLinks.get(0).getGeometry();
            for (IxPoi poi : pois) {
                // 判断Poi引导坐标所处线段几何是否发生变化
                Geometry oldPoint = GeoTranslator.point2Jts(poi.getxGuide(), poi.getyGuide());
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(oldPoint, 0.00001, 5).intersects(nochangeGeo)) continue;

                Coordinate coor = null;
                // 计算poi坐标与移动后link几何最近的点
                coor = GeometryUtils.GetNearestPointOnLine(oldPoint.getCoordinate(), linkGeo);
                if (null != coor) {
                    poi.changedFields().put("xGuide", coor.x);
                    poi.changedFields().put("yGuide", coor.y);
                    result.insertObject(poi, ObjStatus.UPDATE, poi.pid());
                }
            }
        } else if (newLinks.size() > 1) {
            // 跨图幅打断时计算poi与每条RdLink的距离，取距离最小的link为引导link
            for (IxPoi poi : pois) {
                Coordinate minCoor = null;
                int minLinkPid = 0;
                double minLength = 0;
                Geometry oldPoint = GeoTranslator.point2Jts(poi.getxGuide(), poi.getyGuide());

                // 判断Poi引导坐标所处线段几何是否发生变化
                List<Geometry> geometries = new ArrayList<Geometry>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries)));
                if (GeoTranslator.transform(oldPoint, 0.00001, 5).intersects(nochangeGeo)) continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(oldPoint.getCoordinate(), linkGeo);
                    if (null != tmpCoor) {
                        double length = GeometryUtils.getDistance(oldPoint.getCoordinate(), tmpCoor);
                        if (minLength == 0 || length < minLength) {
                            minLength = length;
                            minCoor = tmpCoor;
                            minLinkPid = link.pid();
                        }
                    }
                }
                if (null != minCoor) {
                    poi.changedFields().put("xGuide", minCoor.x);
                    poi.changedFields().put("yGuide", minCoor.y);
                    poi.changedFields().put("linkPid", minLinkPid);
                    result.insertObject(poi, ObjStatus.UPDATE, poi.pid());
                }
            }
        }
    }
}
