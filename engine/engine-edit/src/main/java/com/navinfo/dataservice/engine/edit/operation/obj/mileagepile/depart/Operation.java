package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;
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
     * 用于节点分离维护里程桩
     *
     * @param oldLink  原始RdLink
     * @param newLinks 分离后RdLink
     * @param result   结果集
     * @return
     * @throws Exception
     */
    public String depart(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载RdHgwgLimitSelector
        RdMileagepileSelector selector = new RdMileagepileSelector(this.conn);
        // 获取分离link上挂接的RdHgwgLimit
        List<RdMileagepile> mileagepiles = selector.loadByLinkPid(oldLink.pid(), true);
        // 分离后没产生跨图幅打断
        if (newLinks.size() == 1) {
            Geometry linkGeo = GeoTranslator.transform(newLinks.get(0).getGeometry(), 0.00001, 5);
            for (RdMileagepile mileagepile : mileagepiles) {
                // 判断里程桩所处段几何是否发生变化
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(linkGeo);
                if (GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
                    continue;
                // 计算rdHgwgLimit几何与移动后link几何最近的点
                Coordinate coor = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).getCoordinate(), linkGeo);
                if (null != coor) {
                    mileagepile.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts(coor.x, coor.y)));
                    result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
                }
            }
        } else if (newLinks.size() > 1) {
            // 跨图幅打断时计算rdHgwgLimit与每条RdLink的距离，取距离最小的link为关联link
            for (RdMileagepile mileagepile : mileagepiles) {
                Coordinate minCoor = null;
                int minLinkPid = 0;
                double minLength = 0;
                Coordinate eyeCoor = GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).getCoordinate();
                // 判断里程桩所处段几何是否发生变化
                List<Geometry> geometries = new ArrayList<>();
                for (RdLink link : newLinks) {
                    geometries.add(link.getGeometry());
                }
                Geometry nochangeGeo = GeoTranslator.transform(oldLink.getGeometry(), 0.00001, 5).intersection(GeoTranslator.geojson2Jts(GeometryUtils.connectLinks(geometries), 0.00001, 5));
                if (GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).intersects(nochangeGeo))
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
                    mileagepile.changedFields().put("geometry", GeoTranslator.jts2Geojson(GeoTranslator.point2Jts(minCoor.x, minCoor.y)));
                    mileagepile.changedFields().put("linkPid", minLinkPid);
                    result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
                }
            }
        }
        return null;
    }

    /**
     * 维护上下线分离时里程桩的影响
     *
     * @param sNode      起始点
     * @param links      目标LINK
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(RdNode sNode, List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        // 查找上下线分离对影响到的里程桩
        List<Integer> linkPids = new ArrayList<>();
        linkPids.addAll(leftLinks.keySet());
        RdMileagepileSelector selector = new RdMileagepileSelector(conn);
        List<RdMileagepile> mileagepiles = selector.loadByLinkPids(linkPids, true);
        // 里程桩数量为零则不需要维护
        if (mileagepiles.size() == 0) {
            return "";
        }
        // 构建RdLinkPid-里程桩的对应集合
        Map<Integer, List<RdMileagepile>> mileagepileMap = new HashMap<Integer, List<RdMileagepile>>();
        for (RdMileagepile mileagepile : mileagepiles) {
            List<RdMileagepile> list = mileagepileMap.get(mileagepile.getLinkPid());
            if (null != list) {
                list.add(mileagepile);
            } else {
                list = new ArrayList<>();
                list.add(mileagepile);
                mileagepileMap.put(mileagepile.getLinkPid(), list);
            }
        }
        for (RdLink link : links) {
            RdLink leftLink = leftLinks.get(link.pid());
            RdLink rightLink = rightLinks.get(link.pid());
            if (mileagepileMap.containsKey(link.getPid())) {
                List<RdMileagepile> mileagepiles1 = mileagepileMap.get(link.getPid());
                for (RdMileagepile mileagepile : mileagepiles1) {
                    int direct = mileagepile.getDirect();
                    if (2 == direct)
                        // 里程桩为顺方向则关联link为右线
                        updateMileagepile(rightLink, mileagepile, result);
                    else if (0 == direct || 3 == direct)
                        // 里程桩为逆方向则关联link为左线
                        updateMileagepile(leftLink, mileagepile, result);
                }
            }
        }
        return "";
    }

    // 更新里程桩信息
    private void updateMileagepile(RdLink link, RdMileagepile mileagepile, Result result) throws Exception {
        // 计算原里程桩坐标到分离后link的垂足点
        Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(mileagepile.getGeometry(), 0.00001, 5).getCoordinate(), GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        mileagepile.changedFields().put("geometry", geoPoint);
        mileagepile.changedFields().put("linkPid", link.getPid());
        mileagepile.changedFields().put("direct", link.getDirect());
        // 更新里程桩坐标以及挂接线
        result.insertObject(mileagepile, ObjStatus.UPDATE, mileagepile.pid());
    }
}
