package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Geometry linkGeo = GeoTranslator.transform(newLinks.get(0).getGeometry(), 0.00001, 5);
            for (RdElectroniceye rdElectroniceye : electroniceyes) {
                // 判断电子眼所处段几何是否发生变化
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;
                // 计算rdElectroniceye几何与移动后link几何最近的点
                Coordinate coor = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).getCoordinate(), linkGeo);
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
                Coordinate eyeCoor = GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).getCoordinate();
                // 判断电子眼所处段几何是否发生变化
                List<Geometry> geometries = new ArrayList<Geometry>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries), 0.00001, 5));
                if (GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(eyeCoor, GeoTranslator.transform(linkGeo, 0.00001, 5));
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

    /**
     * 用于维护上下线分离对电子眼的影响
     *
     * @param sNode      起始点PID
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        // 查找上下线分离对影响到的电子眼
        List<Integer> linkPids = new ArrayList<Integer>();
        linkPids.addAll(leftLinks.keySet());
        RdElectroniceyeSelector rdElectroniceyeSelector = new RdElectroniceyeSelector(conn);
        List<RdElectroniceye> rdElectroniceyes = rdElectroniceyeSelector.loadListByRdLinkIds(linkPids, true);
        // 电子眼数量为零则不需要维护
        if (rdElectroniceyes.size() != 0) {
            // 构建RdLinkPid-电子眼的对应集合
            Map<Integer, List<RdElectroniceye>> rdElectroniceyeMap = new HashMap<Integer, List<RdElectroniceye>>();
            for (RdElectroniceye rdElectroniceye : rdElectroniceyes) {
                List<RdElectroniceye> list = rdElectroniceyeMap.get(rdElectroniceye.getLinkPid());
                if (null != list) {
                    list.add(rdElectroniceye);
                } else {
                    list = new ArrayList<RdElectroniceye>();
                    list.add(rdElectroniceye);
                    rdElectroniceyeMap.put(rdElectroniceye.getLinkPid(), list);
                }
            }
            for (RdLink link : links) {
                RdLink leftLink = leftLinks.get(link.pid());
                RdLink rightLink = rightLinks.get(link.pid());
                if (rdElectroniceyeMap.containsKey(link.getPid())) {
                    List<RdElectroniceye> rdElectroniceyeList = rdElectroniceyeMap.get(link.getPid());
                    for (RdElectroniceye rdElectroniceye : rdElectroniceyeList) {
                        int direct = rdElectroniceye.getDirect();
                        if (2 == direct)
                            // 电子眼为顺方向则关联link为右线
                            updateRdElectroniceye(rightLink, rdElectroniceye, result);
                        else if (3 == direct)
                            // 电子眼为逆方向则关联link为左线
                            updateRdElectroniceye(leftLink, rdElectroniceye, result);
                    }
                }
            }
        }
        // 维护非目标Link的信息
        for (Map.Entry<Integer, RdLink> entry : noTargetLinks.entrySet()) {
            int linkPid = entry.getKey();
            List<RdElectroniceye> electroniceyes = rdElectroniceyeSelector.loadListByRdLinkId(linkPid, true);
            RdLink sourceLink = entry.getValue();
            RdLink link = new RdLink();
            link.copy(sourceLink);

            Geometry newGeo = null;
            if (sourceLink.changedFields().containsKey("geometry")) {
                newGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(sourceLink.changedFields().get("geometry")), 100000, 5);
            }
            if (null == newGeo || newGeo.isEmpty())
                continue;
            else {
                link.setPid(sourceLink.pid());
                link.setGeometry(newGeo);
            }
            for (RdElectroniceye electroniceye : electroniceyes) {
                updateRdElectroniceye(link, electroniceye, result);
            }
        }
        return "";
    }

    // 更新电子眼信息
    private void updateRdElectroniceye(RdLink link, RdElectroniceye rdElectroniceye, Result result) throws Exception {
        // 计算原电子眼坐标到分离后link的垂足点
        Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(rdElectroniceye.getGeometry(), 0.00001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        rdElectroniceye.changedFields().put("geometry", geoPoint);
        rdElectroniceye.changedFields().put("linkPid", link.getPid());
        if (link.getDirect() != 1)
            rdElectroniceye.changedFields().put("direct", link.getDirect());
        // 更新电子眼坐标以及挂接线
        result.insertObject(rdElectroniceye, ObjStatus.UPDATE, rdElectroniceye.pid());
    }
}
