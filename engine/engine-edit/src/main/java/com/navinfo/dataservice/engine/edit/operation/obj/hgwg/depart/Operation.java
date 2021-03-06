package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;
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
 * Created by chaixin on 2016/11/11 0011.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 用于节点分离维护限高限重
     *
     * @param oldLink  原始RdLink
     * @param newLinks 分离后RdLink
     * @param result   结果集
     * @return
     * @throws Exception
     */
    public String depart(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载RdHgwgLimitSelector
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(this.conn);
        // 获取分离link上挂接的RdHgwgLimit
        List<RdHgwgLimit> hgwgLimits = selector.loadByLinkPid(oldLink.pid(), true);
        // 分离后没产生跨图幅打断
        if (newLinks.size() == 1) {
            Geometry linkGeo = GeoTranslator.transform(newLinks.get(0).getGeometry(), 0.00001, 5);
            for (RdHgwgLimit rdHgwgLimit : hgwgLimits) {
                // 判断限高限重所处段几何是否发生变化
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(rdHgwgLimit.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;
                // 计算rdHgwgLimit几何与移动后link几何最近的点
                Coordinate coor = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(rdHgwgLimit.getGeometry
                        (), 0.00001, 5).getCoordinate(), linkGeo);
                if (null != coor) {
                    rdHgwgLimit.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts
                            (coor.x, coor.y)));
                    result.insertObject(rdHgwgLimit, ObjStatus.UPDATE, rdHgwgLimit.pid());
                }
            }
        } else if (newLinks.size() > 1) {
            // 跨图幅打断时计算rdHgwgLimit与每条RdLink的距离，取距离最小的link为关联link
            for (RdHgwgLimit rdHgwgLimit : hgwgLimits) {
                Coordinate minCoor = null;
                int minLinkPid = 0;
                double minLength = 0;
                Coordinate eyeCoor = GeoTranslator.transform(rdHgwgLimit.getGeometry(), 0.00001, 5).getCoordinate();
                // 判断限高限重所处段几何是否发生变化
                List<Geometry> geometries = new ArrayList<>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection
                        (GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries), 0.00001, 5));
                if (GeoTranslator.transform(rdHgwgLimit.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
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
                    rdHgwgLimit.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts
                            (minCoor.x, minCoor.y)));
                    rdHgwgLimit.changedFields().put("linkPid", minLinkPid);
                    result.insertObject(rdHgwgLimit, ObjStatus.UPDATE, rdHgwgLimit.pid());
                }
            }
        }
        return null;
    }

    /**
     * 维护上下线分离时限高限重的影响
     *
     * @param sNode      起始点
     * @param links      目标LINK
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink>
            rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        // 查找上下线分离对影响到的限高限重
        List<Integer> linkPids = new ArrayList<>();
        linkPids.addAll(leftLinks.keySet());
        RdHgwgLimitSelector rdHgwgLimitSelector = new RdHgwgLimitSelector(conn);
        List<RdHgwgLimit> hgwgLimits = rdHgwgLimitSelector.loadByLinkPids(linkPids, true);

        int sNodePid = sNode.pid();
        Map<Integer, Integer> maps = new HashMap<>();
        for (RdLink link : links) {
            if (sNodePid == link.getsNodePid()) {
                maps.put(link.pid(), 2);
                sNodePid = link.geteNodePid();
            } else if (sNodePid == link.geteNodePid()) {
                maps.put(link.pid(), 3);
                sNodePid = link.getsNodePid();
            }
        }
        // 限高限重数量为零则不需要维护
        if (hgwgLimits.size() != 0) {
            // 构建RdLinkPid-限高限重的对应集合
            Map<Integer, List<RdHgwgLimit>> rdHgwgMap = new HashMap<>();
            for (RdHgwgLimit hgwg : hgwgLimits) {
                List<RdHgwgLimit> list = rdHgwgMap.get(hgwg.getLinkPid());
                if (null != list) {
                    list.add(hgwg);
                } else {
                    list = new ArrayList<>();
                    list.add(hgwg);
                    rdHgwgMap.put(hgwg.getLinkPid(), list);
                }
            }
            for (RdLink link : links) {
                RdLink leftLink = leftLinks.get(link.pid());
                RdLink rightLink = rightLinks.get(link.pid());
                if (rdHgwgMap.containsKey(link.getPid())) {
                    List<RdHgwgLimit> rdHgwgList = rdHgwgMap.get(link.getPid());
                    for (RdHgwgLimit rdHgwgLimit : rdHgwgList) {
                        int direct = rdHgwgLimit.getDirect();
                        int opDirect = maps.get(rdHgwgLimit.getLinkPid());
                        if (2 == direct) {
                            if (opDirect == 2) {
                                // 限高限重为顺方向、分离为逆方向则关联link为右线
                                updateRdHgwgLimit(rightLink, rdHgwgLimit, result);
                            } else if (opDirect == 3) {
                                // 限高限重为顺方向、分离为逆方向则关联link为右线
                                updateRdHgwgLimit(leftLink, rdHgwgLimit, result);
                            }
                        } else if (3 == direct) {
                            if (opDirect == 2) {
                                // 限高限重为逆方向、分离为顺方向则关联link为左线
                                updateRdHgwgLimit(leftLink, rdHgwgLimit, result);
                            } else if (opDirect == 3) {
                                // 限高限重为逆方向、分离为顺方向则关联link为左线
                                updateRdHgwgLimit(rightLink, rdHgwgLimit, result);
                            }
                        }
                    }
                }
            }
        }
        // 维护非目标Link的信息
        for (Map.Entry<Integer, RdLink> entry : noTargetLinks.entrySet()) {
            int linkPid = entry.getKey();
            List<RdHgwgLimit> hgwgs = rdHgwgLimitSelector.loadByLinkPid(linkPid, true);
            RdLink sourceLink = entry.getValue();
            RdLink link = new RdLink();
            link.copy(sourceLink);

            Geometry newGeo = null;
            if (sourceLink.changedFields().containsKey("geometry")) {
                newGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(sourceLink.changedFields().get("geometry")),
                        100000, 5);
            }
            if (null == newGeo || newGeo.isEmpty())
                continue;
            else {
                link.setPid(sourceLink.pid());
                link.setGeometry(newGeo);
            }
            for (RdHgwgLimit hgwg : hgwgs) {
                updateRdHgwgLimit(link, hgwg, result);
            }
        }
        return "";
    }

    // 更新限高限重信息
    private void updateRdHgwgLimit(RdLink link, RdHgwgLimit hgwgLimit, Result result) throws Exception {
        // 计算原限高限重坐标到分离后link的垂足点
        Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(hgwgLimit.getGeometry(),
                0.00001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        hgwgLimit.changedFields().put("geometry", geoPoint);
        hgwgLimit.changedFields().put("linkPid", link.getPid());
        if (link.getDirect() != 1)
            hgwgLimit.changedFields().put("direct", link.getDirect());
        // 更新限高限重坐标以及挂接线
        result.insertObject(hgwgLimit, ObjStatus.UPDATE, hgwgLimit.pid());
    }
}
