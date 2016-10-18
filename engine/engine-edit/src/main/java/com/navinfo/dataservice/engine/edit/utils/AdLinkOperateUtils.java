package com.navinfo.dataservice.engine.edit.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhaokk LINK 公共方法
 */
public class AdLinkOperateUtils {

    /*
     * 添加link获取下一条连接的link
     */
    public static boolean getNextLink(List<AdLink> links, Map<Integer, AdLink> map) throws Exception {
        int nextNodePid = 0;
        int currNodePid = map.keySet().iterator().next();
        AdLink currLink = map.get(map.keySet().iterator().next());
        if (currNodePid == currLink.getsNodePid()) {
            nextNodePid = currLink.geteNodePid();
        } else {
            nextNodePid = currLink.getsNodePid();
        }
        for (AdLink link : links) {
            if (link.getPid() == currLink.getPid()) {
                continue;
            }
            if (link.getsNodePid() == nextNodePid
                    || link.geteNodePid() == nextNodePid) {
                map.clear();
                map.put(nextNodePid, link);
                return true;
            }
        }
        return false;
    }

    /*
     * 创建生成一条ADLINK
     * */
    public static void addLink(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
        AdLink link = new AdLink();
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyAdLinkPid());
        if (meshes.size() == 2) {
            link.setKind(0);
        }
        Iterator<String> it = meshes.iterator();
        List<IRow> meshIRows = new ArrayList<IRow>();
        while (it.hasNext()) {
            meshIRows.add(getLinkChildren(link, Integer.parseInt(it.next())));
        }
        link.setMeshes(meshIRows);
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.setPrimaryPid(link.pid());
        result.insertObject(link, ObjStatus.INSERT, link.pid());
    }


    /*
     * 创建生成一条ADLINK返回
     * */
    public static AdLink getAddLink(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
        AdLink link = new AdLink();
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyAdLinkPid());
        //判断是否假象线
        if (MeshUtils.isMeshLine(g)) {
            link.setKind(0);
        }
        Iterator<String> it = meshes.iterator();
        List<IRow> meshIRows = new ArrayList<IRow>();
        while (it.hasNext()) {
            meshIRows.add(getLinkChildren(link, Integer.parseInt(it.next())));
        }
        link.setMeshes(meshIRows);
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.setPrimaryPid(link.pid());
        result.insertObject(link, ObjStatus.INSERT, link.pid());
        return link;
    }

    /*
     * 创建生成一条ADLINK
     * 继承原有LINK的属性
     * */
    public static IRow addLinkBySourceLink(Geometry g, int sNodePid, int eNodePid, AdLink sourcelink, Result result) throws Exception {
        AdLink link = new AdLink();
        link.copy(sourcelink);
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyAdLinkPid());
        if (meshes.size() == 2) {
            link.setKind(0);
        }
        Iterator<String> it = meshes.iterator();
        List<IRow> meshIRows = new ArrayList<IRow>();
        while (it.hasNext()) {
            meshIRows.add(getLinkChildren(link, Integer.parseInt(it.next())));
        }
        link.setMeshes(meshIRows);
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.insertObject(link, ObjStatus.INSERT, link.pid());
        return link;
    }


    /*
     * 维护link的子表 AD_LINK_MESH
     *
     * @param link
     */
    private static AdLinkMesh getLinkChildren(AdLink link, int meshId) {

        AdLinkMesh mesh = new AdLinkMesh();

        mesh.setLinkPid(link.getPid());
        mesh.setMesh(meshId);
        mesh.setMeshId(meshId);
        return mesh;
    }

    /*
     * 创建一条行政区划线对应的端点
     */
    public static JSONObject createAdNodeForLink(Geometry g, int sNodePid, int eNodePid, Result result)
            throws Exception {
        JSONObject node = new JSONObject();
        if (0 == sNodePid) {
            Coordinate point = g.getCoordinates()[0];
            AdNode adNode = NodeOperateUtils.createAdNode(point.x, point.y);
            result.insertObject(adNode, ObjStatus.INSERT, adNode.pid());
            node.put("s", adNode.getPid());
        } else {
            node.put("s", sNodePid);
        }
        //创建终止点信息
        if (0 == eNodePid) {
            Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
            AdNode adNode = NodeOperateUtils.createAdNode(point.x, point.y);
            result.insertObject(adNode, ObjStatus.INSERT, adNode.pid());
            node.put("e", adNode.getPid());
        } else {
            node.put("e", eNodePid);
        }
        return node;

    }

    /*
     * 分割行政区划线
     *
     * @param geometry 要分割线的几何 sNodePid 起点pid eNodePid 终点pid catchLinks
     * 挂接的线和点的集合 1.生成所有不存在的RDNODE 2.标记挂接的link被打断的点 3.返回线被分割的几何属性和起点和终点的List集合
     */
    public static Map<Geometry, JSONObject> splitLink(Geometry geometry, int sNodePid,
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
        // 生成起点ADNODE
        if (0 == sNodePid) {
            double x = coordinates.getJSONArray(0).getDouble(0);

            double y = coordinates.getJSONArray(0).getDouble(1);

            AdNode node = NodeOperateUtils.createAdNode(x, y);
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
        //如果挂接只有ADNODE则不需要生成新的ADNODE
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

                    AdNode node = NodeOperateUtils.createAdNode(x, y);

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

                AdNode node = NodeOperateUtils.createAdNode(x, y);

                result.insertObject(node, ObjStatus.INSERT, node.pid());

                se.put("e", node.getPid());
            }
            maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
        }
        return maps;

    }

    /*
     * 根据移动link端点重新生成link的几何
     */
    public static Geometry caleLinkGeomertyForMvNode(AdLink link, int nodePid, double lon, double lat) throws JSONException {
        Geometry geom = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
        Coordinate[] cs = geom.getCoordinates();
        double[][] ps = new double[cs.length][2];

        for (int i = 0; i < cs.length; i++) {
            ps[i][0] = cs[i].x;

            ps[i][1] = cs[i].y;
        }

        if (link.getsNodePid() == nodePid) {
            ps[0][0] = lon;

            ps[0][1] = lat;
        } else {
            ps[ps.length - 1][0] = lon;

            ps[ps.length - 1][1] = lat;
        }
        JSONObject geojson = new JSONObject();

        geojson.put("type", "LineString");

        geojson.put("coordinates", ps);
        return (GeoTranslator.geojson2Jts(geojson, 1, 5));
    }
    /*
     * 创建行政区划线 针对跨图幅有两种情况
	 * 1.跨图幅和图幅交集是LineString 
	 * 2.跨图幅和图幅交集是MultineString
	 * 3.跨图幅需要生成和图廓线的交点
	 */

    public static void createAdLinkWithMesh(Geometry g,
                                            Map<Coordinate, Integer> maps, Result result) throws Exception {
        if (g != null) {

            if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
                AdLinkOperateUtils.calAdLinkWithMesh(g, maps, result);
            }
            if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    AdLinkOperateUtils.calAdLinkWithMesh(g.getGeometryN(i), maps, result);
                }

            }
        }
    }

    public static List<AdLink> getCreateAdLinksWithMesh(Geometry g,
                                                        Map<Coordinate, Integer> maps, Result result) throws Exception {
        List<AdLink> links = new ArrayList<AdLink>();
        if (g != null) {
            if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
                links.add(AdLinkOperateUtils.getCalAdLinkWithMesh(g, maps, result));
            }
            if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    links.add(AdLinkOperateUtils.getCalAdLinkWithMesh(g.getGeometryN(i), maps, result));
                }

            }
            if (GeometryTypeName.GEOMETRYCOLLECTION.equals(g.getGeometryType())) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    Geometry geometry = g.getGeometryN(i);
                    if (GeometryTypeName.LINESTRING.equals(geometry.getGeometryType())) {
                        links.add(AdLinkOperateUtils.getCalAdLinkWithMesh(geometry, maps, result));
                    }
                }
            }
        }
        return links;
    }

    /*
     * 创建行政区划线 针对跨图幅创建图廓点不能重复
     */
    public static void calAdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps,
                                         Result result) throws Exception {
        //定义创建行政区划线的起始Pid 默认为0
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
        JSONObject node = AdLinkOperateUtils.createAdNodeForLink(
                g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        //创建线
        AdLinkOperateUtils.addLink(g, (int) node.get("s"),
                (int) node.get("e"), result);
    }

    /*
     * 创建行政区划线 针对跨图幅创建图廓点不能重复
     */
    public static AdLink getCalAdLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps,
                                              Result result) throws Exception {
        //定义创建行政区划线的起始Pid 默认为0
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
        JSONObject node = AdLinkOperateUtils.createAdNodeForLink(
                g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        //创建线
        return AdLinkOperateUtils.getAddLink(g, (int) node.get("s"),
                (int) node.get("e"), result);
    }

}
