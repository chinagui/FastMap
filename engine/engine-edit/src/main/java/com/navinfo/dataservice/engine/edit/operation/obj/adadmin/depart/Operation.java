package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.geo.GeoUtils;
import com.navinfo.navicommons.geo.computation.GeometryRelationUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 用于维护节点分离对行政区划代表点的影响
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对行政区划代表点的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks,
                               Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        AdAdminSelector selector = new AdAdminSelector(conn);
        for (RdLink link : links) {
            RdLink leftLink = leftLinks.get(link.pid());
            RdLink rightLink = rightLinks.get(link.pid());

            List<AdAdmin> adAdmins = selector.loadRowsByLinkId(link.pid(), true);
            // 判断每一个行政区划代表点与分离后左右线的距离，更新距离近的线为进入线
            for (AdAdmin admin : adAdmins) {
                Coordinate coor = GeoTranslator.transform(admin.getGeometry(), 0.00001, 5).getCoordinate();
                Coordinate leftCoor = GeometryUtils.GetNearestPointOnLine(coor, GeoTranslator.transform(leftLink
                        .getGeometry(), 0.00001, 5));
                double leftDistance = GeometryUtils.getDistance(coor, leftCoor);
                Coordinate rightCoor = GeometryUtils.GetNearestPointOnLine(coor, GeoTranslator.transform(rightLink
                        .getGeometry(), 0.00001, 5));
                double rightDistance = GeometryUtils.getDistance(coor, rightCoor);
                int pid = 0;
                double x = 0, y = 0;
                if (leftDistance <= rightDistance) {
                    pid = leftLink.pid();
                } else {
                    pid = rightLink.pid();
                }
                admin.changedFields().put("linkPid", pid);
                if (admin.getGeometry().intersects(link.getGeometry())) {
                    admin.changedFields().put("side", 3);
                } else {
                    Coordinate c = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(admin.getGeometry(),
                            0.0001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
                    JSONObject geojson = new JSONObject();
                    geojson.put("type", "Point");
                    geojson.put("coordinates", new double[]{c.x, c.y});
                    Geometry nearestPointGeo = GeoTranslator.geojson2Jts(geojson);
                    int side = GeometryUtils.calulatPointSideOflink(admin.getGeometry(), link.getGeometry(),
                            nearestPointGeo);
                    admin.changedFields().put("side", side);
                }
                result.insertObject(admin, ObjStatus.UPDATE, admin.pid());
            }
        }
        List<Integer> noTargetLinkPids = new ArrayList<>();
        noTargetLinkPids.addAll(noTargetLinks.keySet());
        List<AdAdmin> rows = selector.loadRowsByLinkPids(noTargetLinkPids, false);
        if (rows.isEmpty())
            return "";

        List<Geometry> linkGeos = new ArrayList<>();
        for (RdLink link : leftLinks.values()) {
            linkGeos.add(GeoTranslator.transform(link.getGeometry(), 0.0001, 5));
        }
        List<RdLink> tmpLinks = new ArrayList<>(rightLinks.values());
        for (int i = tmpLinks.size() - 1; i >= 0; i--) {
            linkGeos.add(GeoTranslator.transform(tmpLinks.get(i).getGeometry(), 0.0001, 5));
        }
        Geometry polygon = GeoTranslator.getCalLineToPython(linkGeos);
        for (AdAdmin admin : rows) {
            Geometry adminGeo = GeoTranslator.transform(admin.getGeometry(), 0.00001, 5);
            if (polygon.contains(adminGeo)) {
                //admin.getLinkPid()
            }
        }
        return "";
    }

}
