package com.navinfo.dataservice.engine.edit.operation.obj.poi.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.geo.GeoUtils;
import com.navinfo.navicommons.geo.computation.GeometryRelationUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            Geometry linkGeo = GeoTranslator.transform(newLinks.get(0).getGeometry(), 0.00001, 5);
            for (IxPoi poi : pois) {
                // 判断Poi引导坐标所处线段几何是否发生变化
                Geometry oldPoint = GeoTranslator.point2Jts(poi.getxGuide(), poi.getyGuide());
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(oldPoint, 0.00001, 5).intersects(nochangeGeo)) continue;

                Coordinate coor = null;
                // 计算poi坐标与移动后link几何最近的点
                coor = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(oldPoint, 0.00001, 5).getCoordinate(), linkGeo)
                ;
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
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries), 0.00001, 5));
                if (oldPoint.intersects(nochangeGeo)) continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(oldPoint.getCoordinate(), GeoTranslator.transform(linkGeo, 0.00001, 5));
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

    /**
     * 维护上下线分离对IxPoi的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        IxPoiSelector selector = new IxPoiSelector(conn);
        for (RdLink link : links) {
            RdLink leftLink = leftLinks.get(link.pid());
            RdLink rightLink = rightLinks.get(link.pid());

            List<IxPoi> pois = selector.loadIxPoiByLinkPid(link.pid(), true);
            // 判断每一个POI与分离后左右线的距离，更新距离近的线为进入线
            for (IxPoi poi : pois) {
                Coordinate coor = GeoTranslator.transform(poi.getGeometry(), 0.00001, 5).getCoordinate();
                Coordinate leftCoor = GeometryUtils.GetNearestPointOnLine(coor, GeoTranslator.transform(leftLink.getGeometry(), 0.00001, 5));
                double leftDistance = GeometryUtils.getDistance(coor, leftCoor);
                Coordinate rightCoor = GeometryUtils.GetNearestPointOnLine(coor, GeoTranslator.transform(rightLink.getGeometry(), 0.00001, 5));
                double rightDistance = GeometryUtils.getDistance(coor, rightCoor);
                int pid = 0;
                double x = 0, y = 0;
                if (leftDistance <= rightDistance) {
                    pid = leftLink.pid();
                    x = leftCoor.x;
                    y = leftCoor.y;
                } else {
                    pid = rightLink.pid();
                    x = rightCoor.x;
                    y = rightCoor.y;
                }
                Geometry poiGeo = GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 1, 5);
                poi.changedFields().put("linkPid", pid);
                poi.changedFields().put("xGuide", poiGeo.getCoordinate().x);
                poi.changedFields().put("yGuide", poiGeo.getCoordinate().y);
                result.insertObject(poi, ObjStatus.UPDATE, poi.pid());
            }
        }
        return "";
    }
}
