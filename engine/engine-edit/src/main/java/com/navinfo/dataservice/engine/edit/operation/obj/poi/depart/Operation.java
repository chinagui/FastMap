package com.navinfo.dataservice.engine.edit.operation.obj.poi.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
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
                Coordinate coor = null;
                // 计算poi坐标与移动后link几何最近的点
                coor = GeometryUtils.GetNearestPointOnLine(poi.getGeometry().getCoordinate(), linkGeo);
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
                Coordinate poiCoor = poi.getGeometry().getCoordinate();
                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(poiCoor, linkGeo);
                    if (null != tmpCoor) {
                        double length = GeometryUtils.getDistance(poiCoor, tmpCoor);
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
