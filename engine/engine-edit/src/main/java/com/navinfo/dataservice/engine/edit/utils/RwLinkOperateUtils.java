package com.navinfo.dataservice.engine.edit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * RWLINK公共方法类
 *
 * @author zhangxiaolong
 */
public class RwLinkOperateUtils {

    /*
     * 创建生成一条RwLink,未赋值图幅号
     * */
    public static RwLink addLink(Geometry geo, int sNodePid, int eNodePid, Result result) throws Exception {
        RwLink link = new RwLink();

        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(geo);

        if (meshes.size() > 1) {
            throw new Exception("创建rwLink失败：对应多个图幅");
        } else {
            link.setMesh(Integer.parseInt(meshes.iterator().next()));
        }

        link.setPid(PidUtil.getInstance().applyRwLinkPid());

        result.setPrimaryPid(link.getPid());

        double linkLength = GeometryUtils.getLinkLength(geo);

        if (linkLength <= 2) {
            throw new Exception("道路link长度应大于2米");
        }

        link.setLength(linkLength);

        link.setGeometry(GeoTranslator.transform(geo, 100000, 0));

        link.setsNodePid(sNodePid);

        link.seteNodePid(eNodePid);

        return link;
    }

    /*
     * 分割线
     *
     * @param geometry 要分割线的几何 sNodePid 起点pid eNodePid 终点pid catchLinks
     * 挂接的线和点的集合 1.生成所有不存在的rwNODE 2.标记挂接的link被打断的点 3.返回线被分割的几何属性和起点和终点的List集合
     */
    public static Map<Geometry, JSONObject> splitRwLink(Geometry geometry, int sNodePid,
                                                        int eNodePid, JSONArray catchLinks, Result result) throws Exception {
        Map<Geometry, JSONObject> maps = new HashMap<Geometry, JSONObject>();
        JSONArray coordinates = GeoTranslator.jts2Geojson(geometry)
                .getJSONArray("coordinates");
        JSONObject tmpGeom = new JSONObject();
        // 组装要生成的link
        tmpGeom.put("type", "LineString");
        JSONArray tmpCs = new JSONArray();
        // 添加第一个点几何
        tmpCs.add(coordinates.get(0));

        int p = 0;

        int pc = 1;
        // 挂接的第一个点是LINK的几何属性第一个点
        if (tmpCs.getJSONArray(0).getDouble(0) == catchLinks.getJSONObject(0)
                .getDouble("lon")
                && tmpCs.getJSONArray(0).getDouble(1) == catchLinks
                .getJSONObject(0).getDouble("lat")) {
            p = 1;
        }
        JSONObject se = new JSONObject();
        // 生成起点RWNODE
        if (0 == sNodePid) {
            double x = coordinates.getJSONArray(0).getDouble(0);

            double y = coordinates.getJSONArray(0).getDouble(1);

            RwNode node = (RwNode) NodeOperateUtils.createNode(x, y, ObjType.RWNODE);
            result.insertObject(node, ObjStatus.INSERT, node.pid());
            se.put("s", node.getPid());

            if (p == 1 && catchLinks.getJSONObject(0).containsKey("linkPid")) {
                catchLinks.getJSONObject(0).put("breakNode", node.getPid());
            }
        } else {
            se.put("s", sNodePid);
        }
        //循环当前要分割LINK的几何 循环挂接的集合
        // 当挂接几何和link的集合有相同的点 生成新的link
        //如果挂接的存在linkPid 则被打断，且生成新的点
        //如果挂接只有RWNODE则不需要生成新的RWNODE
        while (p < catchLinks.size() && pc < coordinates.size()) {
            tmpCs.add(coordinates.getJSONArray(pc));

            if (coordinates.getJSONArray(pc).getDouble(0) == catchLinks
                    .getJSONObject(p).getDouble("lon")
                    && coordinates.getJSONArray(pc).getDouble(1) == catchLinks
                    .getJSONObject(p).getDouble("lat")) {

                tmpGeom.put("coordinates", tmpCs);
                if (catchLinks.getJSONObject(p).containsKey("nodePid")) {
                    se.put("e", catchLinks.getJSONObject(p).getInt("nodePid"));
                    maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
                    se = new JSONObject();

                    se.put("s", catchLinks.getJSONObject(p).getInt("nodePid"));
                } else {
                    double x = catchLinks.getJSONObject(p).getDouble("lon");

                    double y = catchLinks.getJSONObject(p).getDouble("lat");

                    RwNode node = (RwNode) NodeOperateUtils.createNode(x, y, ObjType.RWNODE);

                    result.insertObject(node, ObjStatus.INSERT, node.pid());

                    se.put("e", node.getPid());
                    maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
                    se = new JSONObject();

                    se.put("s", node.getPid());

                    catchLinks.getJSONObject(p).put("breakNode", node.getPid());
                }

                tmpGeom = new JSONObject();

                tmpGeom.put("type", "LineString");

                tmpCs = new JSONArray();

                tmpCs.add(coordinates.getJSONArray(pc));

                p++;
            }

            pc++;
        }
        //循环挂接的线是否完毕 如果>1 则表示完毕
        if (tmpCs.size() > 0 && pc < coordinates.size()) {
            for (int i = pc; i < coordinates.size(); i++) {
                tmpCs.add(coordinates.get(i));
            }

            tmpGeom.put("coordinates", tmpCs);
            if (eNodePid != 0) {
                se.put("e", eNodePid);

            } else {
                double x = tmpCs.getJSONArray(tmpCs.size() - 1).getDouble(0);

                double y = tmpCs.getJSONArray(tmpCs.size() - 1).getDouble(1);

                RwNode node = (RwNode) NodeOperateUtils.createNode(x, y, ObjType.RWNODE);

                result.insertObject(node, ObjStatus.INSERT, node.pid());

                se.put("e", node.getPid());
            }
            maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
        }
        return maps;

    }

    /*
     * 创建一条rwLink对应的端点
     */
    public static JSONObject createRwNodeForLink(Geometry g, int sNodePid, int eNodePid, Result result)
            throws Exception {
        JSONObject node = new JSONObject();
        if (0 == sNodePid) {
            Coordinate point = g.getCoordinates()[0];
            RwNode rwNODE = (RwNode) NodeOperateUtils.createNode(point.x, point.y, ObjType.RWNODE);
            result.insertObject(rwNODE, ObjStatus.INSERT, rwNODE.pid());
            node.put("s", rwNODE.getPid());
        } else {
            node.put("s", sNodePid);
        }
        //创建终止点信息
        if (0 == eNodePid) {
            Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
            RwNode rwNODE = (RwNode) NodeOperateUtils.createNode(point.x, point.y, ObjType.RWNODE);
            result.insertObject(rwNODE, ObjStatus.INSERT, rwNODE.pid());
            node.put("e", rwNODE.getPid());
        } else {
            node.put("e", eNodePid);
        }
        return node;

    }

    /*
     * 创建生成一条RwLink
     * 继承原有LINK的属性
     * */
    public static IRow addLinkBySourceLink(Geometry g, int sNodePid, int eNodePid, RwLink sourcelink, Result result) throws Exception {
        RwLink link = new RwLink();
        link.copy(sourcelink);
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyRwLinkPid());
        if (meshes.size() > 1) {
            throw new Exception("打断生成新RwLink失败：对应多个图幅");
        } else {
            link.setMesh(Integer.parseInt(meshes.iterator().next()));
        }
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.insertObject(link, ObjStatus.INSERT, link.pid());
        result.setPrimaryPid(link.getPid());
        return link;
    }

	/*
	 * 创建线 针对跨图幅有两种情况 
	 * 1.跨图幅和图幅交集是LineString 
	 * 2.跨图幅和图幅交集是MultineString
	 * 3.跨图幅需要生成和图廓线的交点
	 */

    public static void createRwLinkWithMesh(Geometry g,
                                            Map<Coordinate, Integer> maps, Result result) throws Exception {
        if (g != null) {

            if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
                RwLinkOperateUtils.calLinkWithMesh(g, maps, result);
            }
            if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    RwLinkOperateUtils.calLinkWithMesh(g.getGeometryN(i), maps, result);
                }

            }
        }
    }

    /*
     * 创建线 针对跨图幅创建图廓点不能重复
     */
    public static void calLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps,
                                       Result result) throws Exception {
        //定义创建线的起始Pid 默认为0
        int sNodePid = 0;
        int eNodePid = 0;
        //判断新创建的线起点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        //判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        //创建线对应的点
        JSONObject node = RwLinkOperateUtils.createNodeForLink(
                g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        //创建线
        RwLink link = RwLinkOperateUtils.addLink(g, (int) node.get("s"),
                (int) node.get("e"), result);

        result.insertObject(link, ObjStatus.INSERT, link.pid());
    }


    /*
     * 创建线对应的端点
     */
    public static JSONObject createNodeForLink(Geometry g, int sNodePid, int eNodePid, Result result)
            throws Exception {
        JSONObject node = new JSONObject();
        if (0 == sNodePid) {
            Coordinate point = g.getCoordinates()[0];
            RwNode rwNode = NodeOperateUtils.createRwNode(point.x, point.y);
            result.insertObject(rwNode, ObjStatus.INSERT, rwNode.pid());
            node.put("s", rwNode.getPid());
        } else {
            node.put("s", sNodePid);
        }
        //创建终止点信息
        if (0 == eNodePid) {
            Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
            RwNode rwNode = NodeOperateUtils.createRwNode(point.x, point.y);
            result.insertObject(rwNode, ObjStatus.INSERT, rwNode.pid());
            node.put("e", rwNode.getPid());
        } else {
            node.put("e", eNodePid);
        }
        return node;

    }

    /*
     * 创建生成一条RwLINK返回
     * */
    public static RwLink getAddLink(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
        RwLink link = new RwLink();

        link.setPid(PidUtil.getInstance().applyRwLinkPid());

        double linkLength = GeometryUtils.getLinkLength(g);

        link.setLength(linkLength);

        link.setGeometry(GeoTranslator.transform(g, 100000, 0));

        link.setsNodePid(sNodePid);

        link.seteNodePid(eNodePid);

        result.setPrimaryPid(link.pid());

        result.insertObject(link, ObjStatus.INSERT, link.pid());

        return link;
    }

    public static List<RwLink> getCreateRwLinksWithMesh(Geometry g,
                                                        Map<Coordinate, Integer> maps, Result result) throws Exception {
        List<RwLink> links = new ArrayList<RwLink>();
        if (g != null) {
            if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
                links.add(getCalRwLinkWithMesh(g, maps, result));
            }
            if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    links.add(getCalRwLinkWithMesh(g.getGeometryN(i), maps, result));
                }
            }
            if (GeometryTypeName.GEOMETRYCOLLECTION.equals(g.getGeometryType())) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    Geometry geometry = g.getGeometryN(i);
                    if (GeometryTypeName.LINESTRING.equals(geometry.getGeometryType())) {
                        links.add(getCalRwLinkWithMesh(geometry, maps, result));
                    }
                }
            }
        }
        return links;
    }

    /*
     * 创建铁路线 针对跨图幅创建图廓点不能重复
     */
    public static void calRwLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps,
                                         Result result) throws Exception {
        //定义创建铁路线的起始Pid 默认为0
        int sNodePid = 0;
        int eNodePid = 0;
        //判断新创建的线起点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        //判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        //创建线对应的点
        JSONObject node = RwLinkOperateUtils.createNodeForLink(
                g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        //创建线
        RwLink link = RwLinkOperateUtils.addLink(g, (int) node.get("s"),
                (int) node.get("e"), result);

        result.insertObject(link, ObjStatus.INSERT, link.pid());
    }

    /*
     * 创建铁路线 针对跨图幅创建图廓点不能重复
     */
    public static RwLink getCalRwLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps,
                                              Result result) throws Exception {
        //定义创建铁路线的起始Pid 默认为0
        int sNodePid = 0;
        int eNodePid = 0;
        //判断新创建的线起点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        //判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        //创建线对应的点
        JSONObject node = RwLinkOperateUtils.createNodeForLink(
                g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        //创建线
        RwLink link = RwLinkOperateUtils.addLink(g, (int) node.get("s"),
                (int) node.get("e"), result);

        result.insertObject(link, ObjStatus.INSERT, link.pid());

        return link;
    }


}
