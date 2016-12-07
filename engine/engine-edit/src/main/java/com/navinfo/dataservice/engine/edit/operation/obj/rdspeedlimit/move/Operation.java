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
                    Coordinate tmpPoint = this.GetNearestPointOnLine(eleceyeGeo.getCoordinate(), GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5));
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

    /**
     * 计算点与移动后的线之间最近点的坐标
     *
     * @param point
     * @param geom
     * @return
     */
    private Coordinate GetNearestPointOnLine(Coordinate point, Geometry geom) {
        Coordinate[] coll = geom.getCoordinates();

        Coordinate targetPoint = new Coordinate();

        double minDistance = 0;

        if (coll.length < 2) {
            return null;
        }
        targetPoint = coll[0];
        // 计算线段的第一点与限速关系的距离
        minDistance = GeometryUtils.getDistance(point, targetPoint);
        for (int i = 0; i < coll.length - 1; i++) {
            Coordinate point1 = new Coordinate();
            Coordinate point2 = new Coordinate();
            Coordinate pedalPoint = new Coordinate();

            point1 = coll[i];
            point2 = coll[i + 1];

            pedalPoint = GetPedalPoint(point1, point2, point);

            boolean isPointAtLine = IsPointAtLineInter(point1, point2, pedalPoint);

            // 如果在线上
            if (isPointAtLine) {
                double pedalLong = GeometryUtils.getDistance(point, pedalPoint);
                if (pedalLong < minDistance) {
                    minDistance = pedalLong;
                    targetPoint = pedalPoint;
                }
            } else {
                // 计算与点1的最小距离
                double long1 = GeometryUtils.getDistance(point1, point);
                // 计算与点2的最小距离
                double long2 = GeometryUtils.getDistance(point2, point);
                if (long1 <= long2) {
                    if (long1 < minDistance) {
                        minDistance = long1;
                        targetPoint = point1;
                    }
                } else {
                    if (long2 < minDistance) {
                        minDistance = long2;
                        targetPoint = point2;
                    }
                }
            }
        }
        return targetPoint;
    }

    /**
     * 计算垂足点
     */
    private Coordinate GetPedalPoint(Coordinate point1, Coordinate point2, Coordinate point) {
        Coordinate targetPoint = new Coordinate();

        double x1, x2, y1, y2;
        x1 = point1.x;
        y1 = point1.y;
        x2 = point2.x;
        y2 = point2.y;

        if (x1 == x2 && y1 == y2) {
            return null;
        } else if (x1 == x2) {
            targetPoint.x = x1;
            targetPoint.y = point.y;
        } else if (y1 == y2) {
            targetPoint.x = point.x;
            targetPoint.y = y1;
        } else {
            double k = (y2 - y1) / (x2 - x1);
            double x = (k * k * x1 + k * (point.y - y1) + point.x) / (k * k + 1);
            double y = k * (x - x1) + y1;

            targetPoint.x = x;
            targetPoint.y = y;
        }
        return targetPoint;
    }

    /**
     * 判断点point是否在point1和point2组成的线上
     */
    private boolean IsPointAtLineInter(Coordinate point1, Coordinate point2, Coordinate point) {
        boolean result = false;
        double x1, x2, y1, y2, x, y;

        x1 = point1.x;
        y1 = point1.y;
        x2 = point2.x;
        y2 = point2.y;
        x = point.x;
        y = point.y;

        if (x >= min(x1, x2) && x <= max(x1, x2) && y >= min(y1, y2) && y <= max(y1, y2)) {
            result = true;
        }
        return result;
    }

    /**
     * 判断两点的最小值
     *
     * @param x1
     * @param x2
     * @return
     */
    private static double min(double x1, double x2) {
        if (x1 > x2)
            return x2;
        else
            return x1;
    }

    /**
     * 判断两点的最大值
     *
     * @param x1
     * @param x2
     * @return
     */
    private static double max(double x1, double x2) {
        if (x1 < x2)
            return x2;
        else
            return x1;
    }
}
