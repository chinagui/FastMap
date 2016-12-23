package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @author zhangyt
 * @Title: Operation.java
 * @Description: TODO
 * @date: 2016年7月29日 下午3:48:28
 * @version: v1.0
 */
public class Operation implements IOperation {

    public Operation() {
    }

    public Operation(Command command) {
        this.command = command;
    }

    public Operation(Connection conn) {
        this.conn = conn;
    }

    private Command command;

    private Connection conn;

    @Override
    public String run(Result result) throws Exception {
        RdElectroniceye electroniceye = this.command.getEleceye();
        boolean isChanged = electroniceye.fillChangeFields(this.command.getContent());
        if (isChanged) {
            if (command.getLink().getDirect() != 1)
                electroniceye.changedFields().put("direct", command.getLink().getDirect());
            result.insertObject(electroniceye, ObjStatus.UPDATE, electroniceye.pid());
            result.setPrimaryPid(electroniceye.pid());
        }
        return null;
    }

    /**
     * 移动RDNODE时维护电子眼的坐标以及关联Link
     *
     * @param oldLink  电子眼原关联rdlink
     * @param newLinks 新生成的rdlinks
     * @param result
     * @return
     * @throws Exception
     */
    public String moveEleceye(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 查询出所有会被影响的电子眼
        RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.conn);
        List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(oldLink.pid(), true);

        // 定义电子眼是否在新生成的线段上
        boolean isOnTheLine;
        // 循环处理所有电子眼
        for (RdElectroniceye eleceye : eleceyes) {
            isOnTheLine = false;
            for (RdLink link : newLinks) {
                // 判断电子眼的坐标是否在任意一条新生成的线段上
                isOnTheLine = this.isOnTheLine(eleceye.getGeometry(), link.getGeometry());
                // 当电子眼的坐标在新生成的线段时仅维护该电子眼的linkpid属性
                if (isOnTheLine) {
                    eleceye.changedFields().put("linkPid", link.pid());
                    result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
                    break;
                }
            }
            // 当电子眼坐标不再所有的新生成线段
            if (!isOnTheLine) {
                Coordinate minPoint = null;
                double minLength = 0;
                int minLinkPid = 0;
                // 计算电子眼原坐标与新生成线段最近的壹个点的坐标
                Coordinate eleceyeCoor = GeoTranslator.transform(eleceye.getGeometry(), 0.00001, 5).getCoordinate();
                for (RdLink rdLink : newLinks) {
                    Coordinate tmpPoint = this.GetNearestPointOnLine(eleceyeCoor, GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5));
                    double tmpLength = GeometryUtils.getDistance(eleceyeCoor, tmpPoint);
                    if (minLength == 0 || tmpLength < minLength) {
                        minLength = tmpLength;
                        minLinkPid = rdLink.pid();
                        minPoint = tmpPoint;
                    }
                }
                JSONObject geoPoint = new JSONObject();
                geoPoint.put("type", "Point");
                geoPoint.put("coordinates", new double[]{minPoint.x, minPoint.y});
                //				for (RdLink link : newLinks) {
                //					// 判断新生成的坐标处在哪条新生成的线段上并更新电子眼的linkpid字段
                //					if (isOnTheLine(GeoTranslator.geojson2Jts(geoPoint), link.getGeometry())) {
                //						// 更新电子眼的坐标字段
                //						Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);
                //						geoPoint = GeoTranslator.jts2Geojson(tmpGeo, 0.00001, 5);
                //						eleceye.changedFields().put("geometry", geoPoint);
                //
                //						eleceye.changedFields().put("linkPid", link.pid());
                //						result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
                //						break;
                //					}
                //				}
                Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);
                geoPoint = GeoTranslator.jts2Geojson(tmpGeo, 0.00001, 5);
                eleceye.changedFields().put("geometry", geoPoint);
                eleceye.changedFields().put("linkPid", minLinkPid);
                result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
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
        // 计算线段的第一点与电子眼的距离
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
