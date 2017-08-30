package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning.RdLinkWarningSelector;
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
 * Created by ly on 2017/8/18.
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    private RdLinkWarning rdLinkWarning;

    public Operation(Command command) {

        this.command = command;

        this.rdLinkWarning = command.getRdLinkWarning();
    }
    public Operation(Connection conn) {
        this.conn = conn;

    }

    @Override
    public String run(Result result) throws Exception {

        JSONObject content = command.getContent();

        if (content.containsKey("objStatus")
                && ObjStatus.UPDATE.toString().equals(content.getString("objStatus"))) {

            boolean isChanged = rdLinkWarning.fillChangeFields(content);

            if (isChanged) {

                result.insertObject(rdLinkWarning, ObjStatus.UPDATE, rdLinkWarning.pid());
            }
        }

        result.setPrimaryPid(rdLinkWarning.getPid());

        return null;
    }

    /**
     * 删除link维护警示信息
     *
     * @param linkPids 被删linkPids
     */
    public void updateByLinks(List<Integer> linkPids, Result result) throws Exception {

        if (conn == null) {

            return;
        }

        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        List<RdLinkWarning> warnings = selector.loadByLinks(linkPids, true);

        // 更新关联线为零
        for (RdLinkWarning warning : warnings) {

            warning.changedFields().put("linkPid", 0);

            result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
        }
    }

    /**
     * 打断link维护
     *
     * @param oldLink 被打断linkPid
     */
    public void breakRdLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

        if (conn == null) {

            return;
        }

        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);

        List<RdLinkWarning> warnings = selector.loadByLink(oldLink.getPid(), true);

        for (RdLinkWarning warning : warnings) {

            int inLinkPid = 0;

            double distanceFlag = Double.MAX_VALUE;

            for (RdLink link : newLinks) {

                double distance = warning.getGeometry().distance(link.getGeometry());

                if (distance < distanceFlag) {

                    distanceFlag = distance;

                    inLinkPid = link.getPid();
                }
            }

            warning.changedFields().put("linkPid", inLinkPid);

            result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
        }
    }

    public String moveLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 查询出所有会被影响的警示信息
        RdLinkWarningSelector selector = new RdLinkWarningSelector(this.conn);
        List<RdLinkWarning> warnings = selector.loadByLink(oldLink.pid(), true);

        // 循环处理所有限速关系
        for (RdLinkWarning warning : warnings) {

            Coordinate minPoint = null;
            double minLength = Double.MAX_VALUE;
            int minLinkPid = 0;
            // 计算限速关系原坐标与新生成线段最近的壹个点的坐标
            Geometry originalGeo = GeoTranslator.transform(warning.getGeometry(), 0.00001, 5);

            for (RdLink rdLink : newLinks) {
                Coordinate tmpPoint = GeometryUtils.GetNearestPointOnLine(originalGeo.getCoordinate(), GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5));
                double tmpLength = GeometryUtils.getDistance(originalGeo.getCoordinate(), tmpPoint);
                if (tmpLength < minLength) {
                    minLength = tmpLength;
                    minLinkPid = rdLink.pid();
                    minPoint = tmpPoint;
                }
            }

            if (minPoint != null && !originalGeo.getCoordinate().equals(minPoint)) {

                JSONObject geoPoint = new JSONObject();

                geoPoint.put("type", "Point");

                geoPoint.put("coordinates", new double[]{minPoint.x, minPoint.y});

                Geometry tmpGeo = GeoTranslator.geojson2Jts(geoPoint);

                geoPoint = GeoTranslator.jts2Geojson(tmpGeo);

                warning.changedFields().put("geometry", geoPoint);
            }
            if (warning.getLinkPid() != minLinkPid) {

                warning.changedFields().put("linkPid", minLinkPid);
            }
            result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
        }
        return null;
    }

    /**
     * 用于维护上下线分离对警示信息的影响
     */  
    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink>
            rightLinks, Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
        if (links.isEmpty())
            return "";

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

        // 查找上下线分离对影响到的警示信息
        List<Integer> linkPids = new ArrayList<>();
        linkPids.addAll(leftLinks.keySet());
        RdLinkWarningSelector selector = new RdLinkWarningSelector(conn);
        List<RdLinkWarning> warnings = selector.loadByLinks(linkPids, true);
        // 警示信息数量为零则不需要维护
        if (warnings.size() != 0) {
            // 构建RdLinkPid-警示信息的对应集合
            Map<Integer, List<RdLinkWarning>> warningMap = new HashMap<>();
            for (RdLinkWarning warning : warnings) {
                List<RdLinkWarning> list = warningMap.get(warning.getLinkPid());
                if (null != list) {
                    list.add(warning);
                } else {
                    list = new ArrayList<>();
                    list.add(warning);
                    warningMap.put(warning.getLinkPid(), list);
                }
            }
            for (RdLink link : links) {
                RdLink leftLink = leftLinks.get(link.pid());
                RdLink rightLink = rightLinks.get(link.pid());
                if (warningMap.containsKey(link.getPid())) {
                    List<RdLinkWarning> warningList = warningMap.get(link.getPid());
                    for (RdLinkWarning warning : warningList) {
                        int direct = warning.getDirect();
                        int opDirect = maps.get(warning.getLinkPid());
                        if (2 == direct) {
                            if (opDirect == 2) {
                                // 警示信息为顺方向、分离为逆方向则关联link为右线
                                updateRdLinkWarning(rightLink, warning, result);
                            } else if (opDirect == 3) {
                                // 警示信息为顺方向、分离为逆方向则关联link为右线
                                updateRdLinkWarning(leftLink, warning, result);
                            }
                        } else if (3 == direct) {
                            if (opDirect == 2) {
                                // 警示信息为逆方向、分离为顺方向则关联link为左线
                                updateRdLinkWarning(leftLink, warning, result);
                            } else if (opDirect == 3) {
                                // 警示信息为逆方向、分离为顺方向则关联link为左线
                                updateRdLinkWarning(rightLink, warning, result);
                            }
                        }
                    }
                }
            }
        }
        // 维护非目标Link的信息
        for (Map.Entry<Integer, RdLink> entry : noTargetLinks.entrySet()) {
            int linkPid = entry.getKey();
            List<RdLinkWarning> limits = selector.loadByLink(linkPid, true);
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
            for (RdLinkWarning limit : limits) {
                updateRdLinkWarning(link, limit, result);
            }
        }
        return "";
    }

    /**
     * 更新警示信息信息
     */
    private void updateRdLinkWarning(RdLink link, RdLinkWarning warning, Result result) throws Exception {
        // 计算原警示信息坐标到分离后link的垂足点
        Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(warning.getGeometry
                (), 0.00001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        warning.changedFields().put("geometry", geoPoint);
        warning.changedFields().put("linkPid", link.getPid());
        if (link.getDirect() != 1)
            warning.changedFields().put("direct", link.getDirect());
        // 更新警示信息坐标以及挂接线
        result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
    }

    public void depart(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

        RdLinkWarningSelector selector = new RdLinkWarningSelector(this.conn);
        // 获取分离link上挂接的RdLinkWarning
        List<RdLinkWarning> warnings = selector.loadByLink(oldLink.pid(), true);
        // 分离后没产生跨图幅打断
        if (newLinks.size() == 1) {
            Geometry linkGeo = GeoTranslator.transform(newLinks.get(0).getGeometry(), 0.00001, 5);
            for (RdLinkWarning warning : warnings) {
                // 判断点限速所处段几何是否发生变化
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(warning.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;

                // 计算warning几何与移动后link几何最近的点
                Coordinate coor = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(warning.getGeometry
                        (), 0.00001, 5).getCoordinate(), linkGeo);
                if (null != coor) {
                    warning.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts(coor
                            .x, coor.y)));
                    result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
                }
            }
        } else if (newLinks.size() > 1) {
            // 跨图幅打断时计算warning与每条RdLink的距离,取距离最小的link为关联link
            for (RdLinkWarning warning : warnings) {
                Coordinate minCoor = null;
                int minLinkPid = 0;
                double minLength = 0;
                Geometry limitGeo = GeoTranslator.transform(warning.getGeometry(), 0.00001, 5);
                // 判断点限速所处段几何是否发生变化
                List<Geometry> geometries = new ArrayList<>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection
                        (GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries)));
                if (GeoTranslator.transform(warning.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;

                for (RdLink link : newLinks) {
                    Geometry linkGeo = link.getGeometry();
                    Coordinate tmpCoor = GeometryUtils.GetNearestPointOnLine(limitGeo.getCoordinate(), GeoTranslator
                            .transform(linkGeo, 0.00001, 5));
                    if (null != tmpCoor) {
                        double length = GeometryUtils.getDistance(limitGeo.getCoordinate(), tmpCoor);
                        if (minLength == 0 || length < minLength) {
                            minLength = length;
                            minCoor = tmpCoor;
                            minLinkPid = link.pid();
                        }
                    }
                }
                if (null != minCoor) {
                    warning.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts
                            (minCoor.x, minCoor.y)));
                    warning.changedFields().put("linkPid", minLinkPid);
                    result.insertObject(warning, ObjStatus.UPDATE, warning.pid());
                }
            }
        }
    }

}
