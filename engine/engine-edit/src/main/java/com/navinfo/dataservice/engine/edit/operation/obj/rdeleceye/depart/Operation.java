package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于维护节点分离对电子眼的影响
 * Created by chaixin on 2016/9/20 0020.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载RdElectroniceyeSelector
        RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.conn);
        // 获取分离link上挂接的RdElectroniceye
        List<RdElectroniceye> electroniceyes = selector.loadListByRdLinkId(oldLink.pid(), true);
        // 分离后没产生跨图幅打断
        if (newLinks.size() == 1) {
            Geometry linkGeo = newLinks.get(0).getGeometry();
            for (RdElectroniceye rdElectroniceye : electroniceyes) {
                // 判断电子眼所处段几何是否发生变化
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;
                // 计算rdElectroniceye几何与移动后link几何最近的点
                Coordinate coor = GeometryUtils.GetNearestPointOnLine(rdElectroniceye.getGeometry().getCoordinate(), linkGeo);
                if (null != coor) {
                    rdElectroniceye.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts(coor.x, coor.y)));
                    result.insertObject(rdElectroniceye, ObjStatus.UPDATE, rdElectroniceye.pid());
                }
            }
        } else if (newLinks.size() > 1) {
            // 跨图幅打断时计算rdElectroniceye与每条RdLink的距离，取距离最小的link为关联link
            for (RdElectroniceye rdElectroniceye : electroniceyes) {
                Coordinate minCoor = null;
                int minLinkPid = 0;
                double minLength = 0;
                Coordinate eyeCoor = rdElectroniceye.getGeometry().getCoordinate();
                // 判断电子眼所处段几何是否发生变化
                List<Geometry> geometries = new ArrayList<Geometry>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries)));
                if (GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(eyeCoor, linkGeo);
                    if (null != tmpCoor) {
                        double length = GeometryUtils.getDistance(eyeCoor, tmpCoor);
                        if (minLength == 0 || length < minLength) {
                            minLength = length;
                            minCoor = tmpCoor;
                            minLinkPid = link.pid();
                        }
                    }
                }
                if (null != minCoor) {
                    rdElectroniceye.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts(minCoor.x, minCoor.y)));
                    rdElectroniceye.changedFields().put("linkPid", minLinkPid);
                    result.insertObject(rdElectroniceye, ObjStatus.UPDATE, rdElectroniceye.pid());
                }
            }
        }
    }
}
