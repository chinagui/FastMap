package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.List;

/**
 * Created by chaixin on 2016/9/28 0028.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public String moveSpeedlimit(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 查询出所有会被影响的限速关系
        RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.conn);
        List<RdSpeedlimit> speedlimits = selector.loadSpeedlimitByLinkPid(oldLink.pid(), true);

        // 定义限速关系是否在新生成的线段上
        boolean isOnTheLine;
        // 循环处理所有限速关系
        for (RdSpeedlimit speedlimit : speedlimits) {
            isOnTheLine = false;
            for (RdLink link : newLinks) {
                // 判断限速关系的坐标是否在任意一条新生成的线段上
                isOnTheLine = this.isOnTheLine(GeoTranslator.transform(speedlimit.getGeometry(), 0.00001, 5), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
                // 当限速关系的坐标在新生成的线段时仅维护该限速关系的linkpid属性
                if (isOnTheLine) {
                    speedlimit.changedFields().put("linkPid", link.pid());
                    result.insertObject(speedlimit, ObjStatus.UPDATE, speedlimit.pid());
                    break;
                }
            }
            // 当限速关系坐标不再所有的新生成线段
            if (!isOnTheLine) {
                Coordinate minPoint = null;
                double minLength = 0;
                int minLinkPid = 0;
                // 计算限速关系原坐标与新生成线段最近的壹个点的坐标
                Geometry eleceyeGeo = GeoTranslator.transform(speedlimit.getGeometry(), 0.00001, 5);
                for (RdLink rdLink : newLinks) {
                    Coordinate tmpPoint = GeometryUtils.GetNearestPointOnLine(eleceyeGeo.getCoordinate(), GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5));
                    double tmpLength = GeometryUtils.getDistance(eleceyeGeo.getCoordinate(), tmpPoint);
                    if (minLength == 0 || tmpLength < minLength) {
                        minLength = tmpLength;
                        minLinkPid = rdLink.pid();
                        minPoint = tmpPoint;
                    }
                }
                JSONObject geoPoint = new JSONObject();
                geoPoint.put("type", "Point");
                geoPoint.put("coordinates", new double[]{minPoint.x, minPoint.y});
                Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);
                geoPoint = GeoTranslator.jts2Geojson(tmpGeo);
                speedlimit.changedFields().put("geometry", geoPoint);
                speedlimit.changedFields().put("linkPid", minLinkPid);
                result.insertObject(speedlimit, ObjStatus.UPDATE, speedlimit.pid());
            }
        }
        return null;
    }

    /**
     * 判断点是否在线段上
     *
     * @param point 点
     * @param line  线段
     * @return true 是，false 否
     */
    private boolean isOnTheLine(Geometry point, Geometry line) {
        return line.intersects(point);
    }
}
