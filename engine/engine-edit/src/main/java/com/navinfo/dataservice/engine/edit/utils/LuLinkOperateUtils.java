package com.navinfo.dataservice.engine.edit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class LuLinkOperateUtils {

    /**
     * 判断当前Link是否还有下一条与之连通的Link</br>
     *
     * @return 有则返回 true, 没有则返回 false
     */
    public static boolean getNextLink(List<LuLink> links, Map<Integer, LuLink> map) throws Exception {
        int nextNodePid = 0;
        int currNodePid = map.keySet().iterator().next();
        LuLink currLink = map.get(map.keySet().iterator().next());
        if (currNodePid == currLink.getsNodePid()) {
            nextNodePid = currLink.geteNodePid();
        } else {
            nextNodePid = currLink.getsNodePid();
        }
        for (LuLink link : links) {
            if (link.getPid() == currLink.getPid()) {
                continue;
            }
            if (link.getsNodePid() == nextNodePid || link.geteNodePid() == nextNodePid) {
                map.clear();
                map.put(nextNodePid, link);
                return true;
            }
        }
        return false;
    }

    /**
     * 根据线段几何以及起始点创建LuLink
     */
    public static LuLink getLuLink(Geometry g, int sNodePid, int eNodePid, Result result,LuLink sourceLink) throws Exception {
        LuLink link = new LuLink();
        link.setPid(PidUtil.getInstance().applyLuLinkPid());
        if(sourceLink != null)
        {
        	link.copy(sourceLink);
        }
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        Iterator<String> it = meshes.iterator();
        while (it.hasNext()) {
            setLinkChildren(link, Integer.parseInt(it.next()));
        }
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.setPrimaryPid(link.pid());
        result.insertObject(link, ObjStatus.INSERT, link.pid());

        // 创建LuLinkKind
        LuLinkKind kind = new LuLinkKind();
        kind.setLinkPid(link.pid());
        result.insertObject(kind, ObjStatus.INSERT, kind.getLinkPid());
        return link;
    }

    /**
     * 根据线段几何以及起始点创建LuLink</br>
     * 新创建的LuLink集成原sourceLink除Pid外其他属性
     */
    public static IRow addLinkBySourceLink(Geometry g, int sNodePid, int eNodePid, LuLink sourcelink, Result result)
            throws Exception {
        LuLink link = new LuLink();
        link.copy(sourcelink);
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyLuLinkPid());
        Iterator<String> it = meshes.iterator();
        while (it.hasNext()) {
            setLinkChildren(link, Integer.parseInt(it.next()));
        }
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.insertObject(link, ObjStatus.INSERT, link.pid());
        return link;
    }

    /*
     * 维护LuLink子表LuLinkMesh数据
     */
    private static void setLinkChildren(LuLink link, int meshId) {
        LuLinkMesh mesh = new LuLinkMesh();
        mesh.setLinkPid(link.getPid());
        mesh.setMesh(meshId);
        link.getMeshes().add(mesh);
    }

    /**
     * 根据线段几何创建起始点LuNode信息并返回 </br>
     * 注:如起始点结束点已存在则使用原有点 如不存在则创建新的LuNode</br>
     *
     * @return 返回格式{ 's' : startLuNodePid, 'e' : endLuNodePid }
     */
    public static JSONObject createLuNodeForLink(Geometry g, int sNodePid, int eNodePid, Result result)
            throws Exception {
        JSONObject node = new JSONObject();
        if (0 == sNodePid) {
            Coordinate point = g.getCoordinates()[0];
            LuNode luNode = NodeOperateUtils.createLuNode(point.x, point.y);
            result.insertObject(luNode, ObjStatus.INSERT, luNode.pid());
            node.put("s", luNode.getPid());
        } else {
            node.put("s", sNodePid);
        }
        if (0 == eNodePid) {
            Coordinate point = g.getCoordinates()[g.getCoordinates().length - 1];
            LuNode luNode = NodeOperateUtils.createLuNode(point.x, point.y);
            result.insertObject(luNode, ObjStatus.INSERT, luNode.pid());
            node.put("e", luNode.getPid());
        } else {
            node.put("e", eNodePid);
        }
        return node;

    }

    /**
     * 分割土地利用线 1.生成所有不存在的LuNode 2.标记挂接的link被打断的点 3.返回线被分割的几何属性和起点和终点的List集合
     *
     * @param geometry   要分割线的几何</br>
     * @param sNodePid   起点pid
     * @param eNodePid   终点pid
     * @param catchLinks 挂接的线和点的集合
     */
    public static Map<Geometry, JSONObject> splitLink(Geometry geometry, int sNodePid, int eNodePid,
                                                      JSONArray catchLinks, Result result) throws Exception {
        Map<Geometry, JSONObject> maps = new HashMap<Geometry, JSONObject>();
        JSONArray coordinates = GeoTranslator.jts2Geojson(geometry).getJSONArray("coordinates");
        JSONObject tmpGeom = new JSONObject();
        // 组装要生成的Link
        tmpGeom.put("type", "LineString");
        JSONArray tmpCs = new JSONArray();
        // 添加第一个点几何
        tmpCs.add(coordinates.get(0));

        int p = 0;

        int pc = 1;
        // 挂接的第一个点是Link的几何属性第一个点
        if (tmpCs.getJSONArray(0).getDouble(0) == catchLinks.getJSONObject(0).getDouble("lon")
                && tmpCs.getJSONArray(0).getDouble(1) == catchLinks.getJSONObject(0).getDouble("lat")) {
            p = 1;
        }
        JSONObject se = new JSONObject();
        // 生成起点LuNode
        if (0 == sNodePid) {
            double x = coordinates.getJSONArray(0).getDouble(0);

            double y = coordinates.getJSONArray(0).getDouble(1);

            LuNode node = NodeOperateUtils.createLuNode(x, y);
            result.insertObject(node, ObjStatus.INSERT, node.pid());
            se.put("s", node.getPid());

            if (p == 1 && catchLinks.getJSONObject(0).containsKey("linkPid")) {
                catchLinks.getJSONObject(0).put("breakNode", node.getPid());
            }
        } else {
            se.put("s", sNodePid);
        }
        // 循环当前要分割Link的几何 循环挂接的集合
        // 当挂接几何和Link的集合有相同的点 生成新的link
        // 如果挂接的存在linkPid 则被打断，且生成新的点
        // 如果挂接只有LuNode则不需要生成新的LuNode
        while (p < catchLinks.size() && pc < coordinates.size()) {
            tmpCs.add(coordinates.getJSONArray(pc));

            if (coordinates.getJSONArray(pc).getDouble(0) == catchLinks.getJSONObject(p).getDouble("lon")
                    && coordinates.getJSONArray(pc).getDouble(1) == catchLinks.getJSONObject(p).getDouble("lat")) {

                tmpGeom.put("coordinates", tmpCs);
                if (catchLinks.getJSONObject(p).containsKey("nodePid")) {
                    se.put("e", catchLinks.getJSONObject(p).getInt("nodePid"));
                    maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
                    se = new JSONObject();

                    se.put("s", catchLinks.getJSONObject(p).getInt("nodePid"));
                } else {
                    double x = catchLinks.getJSONObject(p).getDouble("lon");

                    double y = catchLinks.getJSONObject(p).getDouble("lat");

                    LuNode node = NodeOperateUtils.createLuNode(x, y);

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
        // 循环挂接的线是否完毕 如果>1 则表示完毕
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

                LuNode node = NodeOperateUtils.createLuNode(x, y);

                result.insertObject(node, ObjStatus.INSERT, node.pid());

                se.put("e", node.getPid());
            }
            maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
        }
        return maps;

    }

    /**
     * 移动LuLink的壹个端点, 根据移动后位置重新生成LuLink的几何模型并返回
     */
    public static Geometry caleLinkGeomertyForMvNode(LuLink link, int nodePid, double lon, double lat)
            throws JSONException {
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
     * 创建行政区划线 针对跨图幅有两种情况 1.跨图幅和图幅交集是LineString 2.跨图幅和图幅交集是MultineString
	 * 3.跨图幅需要生成和图廓线的交点
	 */

    public static void createLuLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result) throws Exception {
        if (g != null) {
            if (g.getGeometryType() == GeometryTypeName.LINESTRING) {
                LuLinkOperateUtils.calLuLinkWithMesh(g, maps, result);
            }
            if (g.getGeometryType() == GeometryTypeName.MULTILINESTRING) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    LuLinkOperateUtils.calLuLinkWithMesh(g.getGeometryN(i), maps, result);
                }
            }
        }
    }

    public static List<LuLink> getCreateLuLinksWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result,LuLink sourceLink)
            throws Exception {
        List<LuLink> links = new ArrayList<LuLink>();
        if (g != null) {
            String geometryType = g.getGeometryType();
            if (GeometryTypeName.LINESTRING.equals(geometryType)) {
                links.add(LuLinkOperateUtils.getCalLuLinkWithMesh(g, maps, result,sourceLink));
            } else if (GeometryTypeName.MULTILINESTRING.equals(geometryType)) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    links.add(LuLinkOperateUtils.getCalLuLinkWithMesh(g.getGeometryN(i), maps, result,sourceLink));
                }

            } else if (GeometryTypeName.GEOMETRYCOLLECTION.equals(geometryType)) {
                for (int i = 0; i < g.getNumGeometries(); i++) {
                    Geometry geo = g.getGeometryN(i);
                    if (GeometryTypeName.LINESTRING.equals(geo.getGeometryType())) {
                        links.add(LuLinkOperateUtils.getCalLuLinkWithMesh(geo, maps, result,sourceLink));
                    }
                }
            }
        }
        return links;
    }

    /*
     * 创建土地利用线
     */
    public static void calLuLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result) throws Exception {
        // 定义创建土地利用线的起始Pid 默认为0
        int sNodePid = 0;
        int eNodePid = 0;
        // 根据对比g的第一个Coordinate判断起始点是否已经存在
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        // 根据对比g的最后一个Coordinate判断终止点是否已经存在
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        // 创建线对应的点
        JSONObject node = LuLinkOperateUtils.createLuNodeForLink(g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        // 创建线
        LuLinkOperateUtils.getLuLink(g, (int) node.get("s"), (int) node.get("e"), result,null);
    }

    /*
     * 创建土地利用线并返回
     */
    public static LuLink getCalLuLinkWithMesh(Geometry g, Map<Coordinate, Integer> maps, Result result,LuLink sourceLink)
            throws Exception {
        // 定义创建行政区划线的起始Pid 默认为0
        int sNodePid = 0;
        int eNodePid = 0;
        // 判断新创建的线起点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[0])) {
            sNodePid = maps.get(g.getCoordinates()[0]);
        }
        // 判断新创建的线终始点对应的pid是否存在，如果存在取出赋值
        if (maps.containsKey(g.getCoordinates()[g.getCoordinates().length - 1])) {
            eNodePid = maps.get(g.getCoordinates()[g.getCoordinates().length - 1]);
        }
        // 创建线对应的点
        JSONObject node = LuLinkOperateUtils.createLuNodeForLink(g, sNodePid, eNodePid, result);
        if (!maps.containsValue(node.get("s"))) {
            maps.put(g.getCoordinates()[0], (int) node.get("s"));
        }
        if (!maps.containsValue(node.get("e"))) {
            maps.put(g.getCoordinates()[g.getCoordinates().length - 1], (int) node.get("e"));
        }
        // 创建线
        return LuLinkOperateUtils.getLuLink(g, (int) node.get("s"), (int) node.get("e"), result,sourceLink);
    }

    /*
     * 创建生成一条LuLink
     */
    public static void addLink(Geometry g, int sNodePid, int eNodePid, Result result) throws Exception {
        LuLink link = new LuLink();
        Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
        link.setPid(PidUtil.getInstance().applyLuLinkPid());
        Iterator<String> it = meshes.iterator();
        while (it.hasNext()) {
            setLinkChildren(link, Integer.parseInt(it.next()));
        }
        double linkLength = GeometryUtils.getLinkLength(g);
        link.setLength(linkLength);
        link.setGeometry(GeoTranslator.transform(g, 100000, 0));
        link.setsNodePid(sNodePid);
        link.seteNodePid(eNodePid);
        result.setPrimaryPid(link.pid());
        result.insertObject(link, ObjStatus.INSERT, link.pid());

        // 创建LuLinkKind
        LuLinkKind kind = new LuLinkKind();
        kind.setLinkPid(link.pid());
        result.insertObject(kind, ObjStatus.INSERT, kind.getLinkPid());
    }

}
