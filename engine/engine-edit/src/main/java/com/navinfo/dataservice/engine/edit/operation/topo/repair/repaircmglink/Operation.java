package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildnodeSelector;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.CmgfaceUtil;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/17
 * @Version: V1.0
 */
public class Operation implements IOperation {

    /**
     * 参数
     */
    private Command command;

    /**
     * 数据库链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    /**
     * 执行操作
     *
     * @param result 操作结果
     * @return 操作后的对象
     * @throws Exception
     */
    @Override
    public String run(Result result) throws Exception {
        // 处理挂接线
        this.caleCatchs(result);
        // 处理修形线
        this.updateLink(result);
        // 处理受影响面
        this.updateFace(result);
        // 处理立交
        this.handleGsc(result);
        return null;
    }

    /**
     * 更新立交信息
     * @param result 结果集
     * @throws Exception 更新立交信息出错
     */
    private void handleGsc(Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation operation =
                new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation();
        Map<Integer, Geometry> newLinkMap = new HashMap<>();
        newLinkMap.put(command.getCmglink().getPid(),
                GeoTranslator.geojson2Jts((JSONObject) command.getCmglink().changedFields().get("geometry")));
        operation.repairLink(this.command.getGscs(), newLinkMap, command.getCmglink(), result);
    }

    private Check check = new Check();
    
    /***
     * 修行挂接点和线
     *
     * @param result 结果集
     * @throws Exception 处理挂接过程出错
     */
    private void caleCatchs(Result result) throws Exception {
    	check.PERMIT_MODIFICATE_POLYGON_ENDPOINT(this.command, this.conn);
        if (!CollectionUtils.isEmpty(command.getCatchInfos())) {
            CmgBuildnodeSelector nodeSelector = new CmgBuildnodeSelector(conn);
            CmgBuildlinkSelector linkSelector = new CmgBuildlinkSelector(conn);
            Iterator<JSONObject> iterator = command.getCatchInfos().iterator();
            while (iterator.hasNext()) {
                JSONObject obj = iterator.next();
                // 分离移动的node
                int nodePid = obj.getInt("nodePid");
                double lon = 0;
                double lat = 0;
                if (!obj.containsKey("catchNodePid")) {
                    // 分离移动后的经纬度
                    lon = CmgnodeUtil.reviseItude(obj.getDouble("longitude"));
                    lat = CmgnodeUtil.reviseItude(obj.getDouble("latitude"));
                }

                CmgBuildnode preNode = (CmgBuildnode) nodeSelector.loadById(nodePid, true, false);
                // 分离node挂接的link
                List<CmgBuildlink> links = linkSelector.listTheAssociatedLinkOfTheNode(nodePid, false);

                if (obj.containsKey("catchNodePid") && obj.getInt("catchNodePid") != 0) {
                    // 分离节点挂接功能
                    this.departCatchtNode(result, obj.getInt("catchNodePid"), preNode, links);
                } else if (obj.containsKey("catchLinkPid") && obj.getInt("catchLinkPid") != 0) {
                    // 分离节点挂接打断功能
                    this.departCatchBreakLink(lon, lat, preNode, obj.getInt("catchLinkPid"), links, result);
                } else {
                    // 移动功能
                    if (links.size() == 1) {
                        this.moveNodeGeo(preNode, lon, lat, result);
                    } else {
                        this.departNode(result, nodePid, lon, lat);
                    }
                }
            }
        }
    }

    /***
     *
     * @param result 结果集
     * @param nodePid 原CMG-NODE
     * @param lon 经度
     * @param lat 纬度
     * @throws Exception 分离节点出错
     */
    private void departNode(Result result, int nodePid, double lon, double lat)
            throws Exception {
        // 分离功能
        CmgBuildnode node = NodeOperateUtils.createCmgBuildnode(lon, lat);
        result.insertObject(node, ObjStatus.INSERT, node.pid());
        this.updateNodeForLink(nodePid, node.pid());

    }

    /***
     *
     *
     * @param node 移动点的对象
     * @param lon 移动后的经度
     * @param lat 移动后的纬度
     * @param result 结果集
     * @throws Exception 移动节点出错
     */
    private void moveNodeGeo(CmgBuildnode node, double lon, double lat, Result result) throws Exception {
        JSONObject geojson = GeoTranslator.jts2Geojson(GeoTranslator.createPoint(new Coordinate(lon, lat)));
        node.changedFields().put("geometry", geojson);
        result.insertObject(node, ObjStatus.UPDATE, node.pid());
    }

    /***
     * 分离节点 修行挂接Node操作
     *
     * @param result 结果集
     * @param catchNodePid 挂接点
     * @param preNode 移动节点
     * @param links 移动点的关联线
     * @throws Exception 处理挂接出错
     */
    private void departCatchtNode(Result result, int catchNodePid, CmgBuildnode preNode, List<CmgBuildlink> links)
            throws Exception {
        CmgBuildnodeSelector nodeSelector = new CmgBuildnodeSelector(conn);
        // 用分离挂接的Node替换修行Link对应的几何,以保持精度
        CmgBuildnode catchNode = (CmgBuildnode) nodeSelector.loadById(catchNodePid, true, true);
        // 获取挂接Node的几乎额
        Geometry geom = GeoTranslator.transform(catchNode.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        Point point = (((Point) GeoTranslator.point2Jts(geom.getCoordinate().x, geom.getCoordinate().y)));
        // 如果原有node挂接的LINK<=1 原来的node需要删除更新link的几何为新的node
        if (links.size() <= 1) {
            result.insertObject(preNode, ObjStatus.DELETE, preNode.getPid());
        }
        // 更新link的几何为新的node点
        this.updateNodeForLink(preNode.pid(), catchNodePid);
        // 更新link的几何用挂接的点的几何代替link的起始形状点
        if (this.command.getCmglink().getsNodePid() == preNode.pid()) {
            this.command.getGeometry().getCoordinates()[0] = point.getCoordinate();
        } else {
            this.command.getGeometry().getCoordinates()[this.command.getGeometry().getCoordinates().length - 1] = point.getCoordinate();
        }
    }

    /***
     * 分离节点 挂接Link打断功能能
     *
     * @param lon 打断点经度
     * @param lat 打断点的维度
     * @param preNode 分离的node
     * @param linkPid 挂节点的linkPid
     * @param links 分离node挂接的node
     * @param result 结果集
     * @throws Exception 打断线出错
     */
    private void departCatchBreakLink(double lon, double lat, CmgBuildnode preNode, int linkPid, List<CmgBuildlink> links, Result result)
            throws Exception {
        // 如果没有挂接的link node移动 如果有node需要新生成
        int breakNodePid = preNode.getPid();
        if (links.size() > 1) {
            this.departNode(result, preNode.pid(), lon, lat);
        } else {
            this.moveNodeGeo(preNode, lon, lat, result);
        }

        // 组装打断的参数
        JSONObject breakJson = new JSONObject();
        breakJson.put("objId", linkPid);
        breakJson.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        data.put("longitude", lon);
        data.put("latitude", lat);
        data.put("breakNodePid", breakNodePid);
        breakJson.put("data", data);
        // 调用打断的API
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command breakCommand =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command(breakJson, breakJson.toString());
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process breakProcess =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process(breakCommand, result, conn);
        breakProcess.innerRun();

    }


    /***
     * 重新赋值link的起始点的pid
     *
     * @param nodePid 原始link的端点pid
     * @param catchNodePid 修行后新的端点pid
     * @throws Exception 分离后修改首尾点
     */
    private void updateNodeForLink(int nodePid, int catchNodePid) throws Exception {
        if (this.command.getCmglink().getsNodePid() == nodePid) {
            command.getCmglink().changedFields().put("sNodePid", catchNodePid);
        } else {
            command.getCmglink().changedFields().put("eNodePid", catchNodePid);
        }
    }

    /**
     * 修改线的信息
     *
     * @param result 结果集
     * @throws Exception 更新线的几何
     */
    private void updateLink(Result result) throws Exception {
        // 修行线不是面组成线时, 更新线的图幅
        if (CollectionUtils.isEmpty(command.getCmgfaces())) {
            Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(command.getGeometry());
            Iterator<IRow> iterator = command.getCmglink().getMeshes().iterator();
            while (iterator.hasNext()) {
                CmgBuildlinkMesh mesh = (CmgBuildlinkMesh) iterator.next();
                if (meshes.contains(String.valueOf(mesh.getMeshId()))) {
                    meshes.remove(String.valueOf(mesh.getMeshId()));
                } else {
                    result.insertObject(mesh, ObjStatus.DELETE, mesh.parentPKValue());
                }
            }
            for (String str : meshes) {
                CmgBuildlinkMesh mesh = new CmgBuildlinkMesh();
                mesh.setLinkPid(command.getCmglink().pid());
                mesh.setMeshId(Integer.parseInt(str));
                result.insertObject(mesh, ObjStatus.INSERT, mesh.parentPKValue());
            }
        }
        // 验证LINK长度
        CmgLinkOperateUtils.validateLength(command.getGeometry());
        command.getCmglink().changedFields().put("geometry", GeoTranslator.jts2Geojson(command.getGeometry()));
        command.getCmglink().changedFields().put("length", GeometryUtils.getLinkLength(command.getGeometry()));
        result.insertObject(command.getCmglink(), ObjStatus.UPDATE, command.getCmglink().pid());
    }

    /**
     * 修改面的信息
     *
     * @param result 结果集
     * @throws Exception 更新面出错
     */
    private void updateFace(Result result) throws Exception {
        if (!CollectionUtils.isEmpty(command.getCmgfaces())) {
            CmgBuildlinkSelector cmglinkSelector = new CmgBuildlinkSelector(conn);
            CmgBuildnodeSelector cmgnodeSelector = new CmgBuildnodeSelector(conn);
            CmgBuildfaceSelector cmgfaceSelector = new CmgBuildfaceSelector(conn);
            for (CmgBuildface cmgface : command.getCmgfaces()) {
                // 更新面的几何信息
                Geometry faceGeo = updateFaceGeo(result, cmglinkSelector, cmgface);

                if (!MeshUtils.mesh2Jts(String.valueOf(cmgface.getMeshId())).intersects(faceGeo)) {
                    int cmgfaceMeshId = CmgfaceUtil.calcFaceMeshId(faceGeo);
                    // 更新面MESH_ID
                    cmgface.changedFields().put("meshId", cmgfaceMeshId);
                    // 更新点MESH_ID
                    List<CmgBuildnode> cmgnodes = cmgnodeSelector.listTheAssociatedNodeOfTheFace(cmgface.pid(), false);
                    updateCmgnodeMesh(result, cmgfaceSelector, cmgface, cmgfaceMeshId, cmgnodes);
                    // 更新线MESH_ID
                    List<CmgBuildlink> cmglinks = cmglinkSelector.listTheAssociatedLinkOfTheFace(cmgface.pid(), false);
                    updateCmglinkMesh(result, cmgfaceSelector, cmgface, cmgfaceMeshId, cmglinks);
                }
            }
        }
    }

    /**
     * 重新维护CMG-LINK-MESH
     * @param result 结果集
     * @param cmgfaceSelector CMG-FACE
     * @param cmgface 关联面
     * @param cmgfaceMeshId 新几何的MESH_ID
     * @param cmglinks 待处理CMG-LINK
     * @throws Exception 维护过程出错
     */
    private void updateCmglinkMesh(Result result, CmgBuildfaceSelector cmgfaceSelector, CmgBuildface cmgface, int cmgfaceMeshId,
                                   List<CmgBuildlink> cmglinks) throws Exception {
        for (CmgBuildlink cmglink : cmglinks) {
            List<CmgBuildface> cmgfaces = cmgfaceSelector.listTheAssociatedFaceOfTheLink(cmglink.pid(), false);
            if (1 == cmgfaces.size()) {
                if (1 == cmglink.getMeshes().size()) {
                    CmgBuildlinkMesh cmglinkMesh = (CmgBuildlinkMesh) cmglink.getMeshes().get(0);
                    cmglinkMesh.changedFields().put("meshId", cmgfaceMeshId);
                    result.insertObject(cmglinkMesh, ObjStatus.UPDATE, cmglink.pid());
                } else {
                    updateSingleCmglinkMesh(result, cmgface, cmgfaceMeshId, cmglink);
                }
            } else {
                if (1 == cmglink.getMeshes().size()) {
                    CmgBuildlinkMesh cmglinkMesh = new CmgBuildlinkMesh();
                    cmglinkMesh.setLinkPid(cmglink.pid());
                    cmglinkMesh.setMeshId(cmgfaceMeshId);
                    result.insertObject(cmglinkMesh, ObjStatus.INSERT, cmglink.pid());
                } else {
                    updateSingleCmglinkMesh(result, cmgface, cmgfaceMeshId, cmglink);
                }
            }
        }
    }

    /**
     * 更新单个CMG-LINK的CMG-LINK-MESH
     * @param result 结果集
     * @param cmgface 关联面
     * @param cmgfaceMeshId 重新生成的MESH_ID
     * @param cmglink 待修改线
     */
    private void updateSingleCmglinkMesh(Result result, CmgBuildface cmgface, int cmgfaceMeshId, CmgBuildlink cmglink) {
        for (IRow row : cmglink.getMeshes()) {
            if (cmgface.mesh() == row.mesh()) {
                row.changedFields().put("meshId", cmgfaceMeshId);
                result.insertObject(row, ObjStatus.UPDATE, cmglink.pid());
                break;
            }
        }
    }

    /**
     * 重新维护CMG-NODE-MESH
     * @param result 结果集
     * @param cmgfaceSelector CMG-FACE
     * @param cmgface 关联面
     * @param cmgfaceMeshId 新几何的MESH_ID
     * @param cmgnodes 待处理CMG-NODE
     * @throws Exception 维护过程出错
     */
    private void updateCmgnodeMesh(Result result, CmgBuildfaceSelector cmgfaceSelector, CmgBuildface cmgface, int cmgfaceMeshId,
                                   List<CmgBuildnode> cmgnodes) throws Exception {
        for (CmgBuildnode cmgnode : cmgnodes) {
            List<CmgBuildface> cmgfaces = cmgfaceSelector.listTheAssociatedFaceOfTheNode(cmgnode.pid(), false);
            if (1 == cmgfaces.size()) {
                if (1 == cmgnode.getMeshes().size()) {
                    CmgBuildnodeMesh cmgnodeMesh = (CmgBuildnodeMesh) cmgnode.getMeshes().get(0);
                    cmgnodeMesh.changedFields().put("meshId", cmgfaceMeshId);
                    result.insertObject(cmgnodeMesh, ObjStatus.UPDATE, cmgnode.pid());
                } else {
                    updateSingleCmgnodeMesh(result, cmgface, cmgfaceMeshId, cmgnode);
                }
            } else {
                if (1 == cmgnode.getMeshes().size()) {
                    CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
                    cmgnodeMesh.setNodePid(cmgnode.pid());
                    cmgnodeMesh.setMeshId(cmgfaceMeshId);
                    result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmgnode.pid());
                } else {
                    updateSingleCmgnodeMesh(result, cmgface, cmgfaceMeshId, cmgnode);
                }
            }
        }
    }

    /**
     * 更新单个CMG-NODE-MESH
     * @param result 结果集
     * @param cmgface 关联面
     * @param cmgfaceMeshId 重新生成的MESH_ID
     * @param cmgnode 待修改CMG-NODE
     */
    private void updateSingleCmgnodeMesh(Result result, CmgBuildface cmgface, int cmgfaceMeshId, CmgBuildnode cmgnode) {
        for (IRow row : cmgnode.getMeshes()) {
            if (cmgface.mesh() == row.mesh()) {
                row.changedFields().put("meshId", cmgfaceMeshId);
                result.insertObject(row, ObjStatus.UPDATE, cmgnode.pid());
                break;
            }
        }
    }

    /**
     *
     * @param result 结果集
     * @param cmglinkSelector CMG-FACE
     * @param cmgface 待更新CMG-FACE面
     * @return 更新后面几何
     * @throws Exception 更新面几何出错
     */
    private Geometry updateFaceGeo(Result result, CmgBuildlinkSelector cmglinkSelector, CmgBuildface cmgface) throws Exception {
        List<IRow> cmgfaceTopos = new AbstractSelector(conn).
                loadRowsByClassParentId(CmgBuildfaceTopo.class, cmgface.pid(), false, "seq_num");
        List<Geometry> geometries = new ArrayList<>();
        for (IRow row : cmgfaceTopos) {
            CmgBuildfaceTopo topo = (CmgBuildfaceTopo) row;
            if (command.getCmglink().pid() == topo.getLinkPid()) {
                geometries.add(command.getGeometry());
                continue;
            }
            CmgBuildlink cmglink = (CmgBuildlink) cmglinkSelector.loadById(topo.getLinkPid(), false);
            geometries.add(GeoTranslator.transform(cmglink.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION));
        }
        Geometry faceGeo = GeoTranslator.getCalLineToPython(geometries);
        cmgface.changedFields().put("geometry", GeoTranslator.jts2Geojson(faceGeo));
        cmgface.changedFields().put("perimeter", GeometryUtils.getLinkLength(faceGeo));
        cmgface.changedFields().put("area", GeometryUtils.getCalculateArea(faceGeo));
        result.insertObject(cmgface, ObjStatus.UPDATE, cmgface.pid());
        return faceGeo;
    }
}