package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.navinfo.navicommons.geo.computation.MyGeoConvertor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Title: CmgLinkOperateUtils
 * @Package: com.navinfo.dataservice.engine.edit.utils
 * @Description: CMG-LINK工具类
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public final class CmgLinkOperateUtils {

    private CmgLinkOperateUtils() {
    }

    /**
     * <b>分割CMG-LINK</b><br>
     * 1.生成所有不存在的RDNODE<br>
     * 2.标记挂接的link被打断的点<br>
     * 3.返回线被分割的几何属性和起点和终点的Map集合
     *
     * @param geometry 要分割线的几何
     * @param sNodePid 起点pid
     * @param eNodePid 终点pid
     * @param catchLinks 挂接的线和点的集合
     * @param result 待处理结果集
     * @throws Exception 打断CMG-LINK出错
     * @return Geometry-线的几何 JSONOBJECT-起始点JSON对象
     */
    public static Map<Geometry, JSONObject> splitLink(Geometry geometry, int sNodePid, int eNodePid,
                                                      JSONArray catchLinks, Result result) throws Exception {
        Map<Geometry, JSONObject> maps = new HashMap<>();

        JSONArray coordinates = GeoTranslator.jts2Geojson(geometry).getJSONArray("coordinates");
        JSONObject tmpGeom = new JSONObject();
        // 组装要生成的link
        tmpGeom.put("type", "LineString");
        JSONArray tmpCs = new JSONArray();
        // 添加第一个点几何
        tmpCs.add(coordinates.get(0));

        int p = 0;
        int pc = 1;
        // 挂接的第一个点是LINK的几何属性第一个点
        if (tmpCs.getJSONArray(0).getDouble(0) == catchLinks.getJSONObject(0).getDouble("lon")
                && tmpCs.getJSONArray(0).getDouble(1) == catchLinks.getJSONObject(0).getDouble("lat")) {
            p = 1;
        }
        JSONObject se = new JSONObject();
        // 生成起点CMG-NODE
        if (0 == sNodePid) {
            double x = coordinates.getJSONArray(0).getDouble(0);
            double y = coordinates.getJSONArray(0).getDouble(1);

            CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(x, y);
            result.insertObject(node, ObjStatus.INSERT, node.pid());
            se.put("s", node.getPid());

            if (p == 1 && catchLinks.getJSONObject(0).containsKey("linkPid")) {
                catchLinks.getJSONObject(0).put("breakNode", node.getPid());
            }
        } else {
            se.put("s", sNodePid);
        }
        // 循环当前要分割LINK的几何 循环挂接的集合
        // 当挂接几何和link的集合有相同的点 生成新的link
        // 如果挂接的存在linkPid 则被打断，且生成新的点
        // 如果挂接只有CMG-NODE则不需要生成新的CMG-NODE
        while (p < catchLinks.size() && pc < coordinates.size()) {
            tmpCs.add(coordinates.getJSONArray(pc));
            double lon = catchLinks.getJSONObject(p).getDouble("lon");
            double lat = catchLinks.getJSONObject(p).getDouble("lat");

            if (coordinates.getJSONArray(pc).getDouble(0) == lon && coordinates.getJSONArray(pc).getDouble(1) == lat) {

                tmpGeom.put("coordinates", tmpCs);
                if (catchLinks.getJSONObject(p).containsKey("nodePid")) {
                    se.put("e", catchLinks.getJSONObject(p).getInt("nodePid"));
                    maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
                    se = new JSONObject();

                    se.put("s", catchLinks.getJSONObject(p).getInt("nodePid"));
                } else {
                    CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(lon, lat);

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
                CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(x, y);
                result.insertObject(node, ObjStatus.INSERT, node.pid());
                se.put("e", node.getPid());
            }
            maps.put(GeoTranslator.geojson2Jts(tmpGeom), se);
        }

        return maps;
    }

    /**
     * 创建一条土地覆盖线对应的端点
     * @param geometry 线几何
     * @param sNodePid 起点PID
     * @param eNodePid 终点PID
     * @param result 结果集
     * @throws Exception 创建起始点出错
     * @return 起、终点JSON对象
     */
    public static JSONObject createCmglinkEndpoint(Geometry geometry, int sNodePid, int eNodePid, Result result) throws Exception {
        JSONObject respon = new JSONObject();
        // 创建起始点信息
        if (0 == sNodePid) {
            Coordinate point = geometry.getCoordinates()[0];
            CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(point.x, point.y);
            result.insertObject(node, ObjStatus.INSERT, node.pid());
            respon.put("s", node.getPid());
        } else {
            respon.put("s", sNodePid);
        }
        // 创建终止点信息
        if (0 == eNodePid) {
            Coordinate point = geometry.getCoordinates()[geometry.getCoordinates().length - 1];
            CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(point.x, point.y);
            result.insertObject(node, ObjStatus.INSERT, node.pid());
            respon.put("e", node.getPid());
        } else {
            respon.put("e", eNodePid);
        }
        return respon;
    }

    /**
     * 创建生成一条CMG-LINK, 同时生成对应CMG-LINK-MESH信息
     * @param geometry 线几何
     * @param sNodePid 起点PID
     * @param eNodePid 终点PID
     * @param result 结果集
     * @param createChild 是否创建子表信息
     * @throws Exception 创建CMG-LINK出错
     * @return 创建的CMG-LINK对象
     */
    public static CmgBuildlink createCmglink(Geometry geometry, int sNodePid, int eNodePid, Result result, boolean createChild) throws Exception {
        CmgBuildlink cmglink = new CmgBuildlink();
        cmglink.setPid(PidUtil.getInstance().applyCmgBuildlinkPid());
        cmglink.setsNodePid(sNodePid);
        cmglink.seteNodePid(eNodePid);
        cmglink.setGeometry(GeoTranslator.transform(geometry, Constant.BASE_EXPAND, 0));
        cmglink.setLength(GeometryUtils.getLinkLength(geometry));
        result.insertObject(cmglink, ObjStatus.INSERT, cmglink.pid());
        if (createChild) {
            createCmglinkChild(cmglink, geometry, result);
        }
        return cmglink;
    }

    /**
     * 创建CMG-LINK子表信息
     * @param cmglink CMG-LINK信息
     * @param geometry CMG-LINK未放大几何
     * @param result 结果集
     * @throws Exception 创建子表信息出错
     */
    private static void createCmglinkChild(CmgBuildlink cmglink, Geometry geometry, Result result) throws Exception {
        cmglink.getMeshes().clear();

        Set<String> meshes = new HashSet<>();
        Coordinate[] cs = geometry.getCoordinates();
        for (int i = 1; i < cs.length; i++) {
            CollectionUtils.addAll(meshes, MeshUtils.line2Meshes(cs[i - 1].x, cs[i - 1].y, cs[i].x, cs[i].y));
        }
        Iterator<String> iterator = meshes.iterator();
        if (meshes.size() > 1) {
            while (iterator.hasNext()) {
                createCmglinkMesh(cmglink, result, iterator);
            }
            if (MeshUtils.isMeshLine(geometry)) {
               cmglink.setKind(0);
            }
        } else {
            createCmglinkMesh(cmglink, result, iterator);
        }
    }

    /**
     * 创建CMG-LINK-MESH
     * @param cmglink 对应CMG-LINK
     * @param result 结果集
     * @param iterator 图幅信息
     */
    private static void createCmglinkMesh(CmgBuildlink cmglink, Result result, Iterator<String> iterator) {
        CmgBuildlinkMesh mesh = new CmgBuildlinkMesh();
        mesh.setLinkPid(cmglink.pid());
        mesh.setMeshId(Integer.valueOf(iterator.next()));
        result.insertObject(mesh, ObjStatus.INSERT, mesh.parentPKValue());
    }

    /*
    * 创建生成一条LCLINK返回
    */
    public static CmgBuildlink createCmglinkBySource(Geometry geometry, int sNodePid, int eNodePid, Result result, CmgBuildlink sourceLink) throws
            Exception {
        CmgBuildlink cmglink = new CmgBuildlink();
        cmglink.setPid(PidUtil.getInstance().applyCmgBuildlinkPid());
        cmglink.copy(sourceLink);
        cmglink.setsNodePid(sNodePid);
        cmglink.seteNodePid(eNodePid);
        cmglink.setGeometry(GeoTranslator.transform(geometry, Constant.BASE_EXPAND, 0));
        cmglink.setLength(GeometryUtils.getLinkLength(geometry));
        result.insertObject(cmglink, ObjStatus.INSERT, cmglink.pid());

        createCmglinkChild(cmglink, geometry, result);
        return cmglink;
    }
}
