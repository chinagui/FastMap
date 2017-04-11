package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
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
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create
 * @Description: 创建CMG-FACE具体操作
 * @Author: Crayeres
 * @Date: 2017/4/11
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
        if (CollectionUtils.isEmpty(command.getLinkPids())) {
            Geometry geometry = GeoTranslator.geojson2Jts(command.getGeometry(), 1, Constant.BASE_PRECISION);
            int cmgfaceMeshId = calcFaceMeshId(geometry);
            // 创建CMG-NODE
            Coordinate firstNode = geometry.getCoordinates()[0];
            CmgBuildnode cmgnode = NodeOperateUtils.createCmgBuildnode(firstNode.x, firstNode.y);
            result.insertObject(cmgnode, ObjStatus.INSERT, cmgnode.pid());
            // 创建CMG-NODE子表MESH
            cmgnode.getMeshes().clear();
            createCmgnodeMesh(result, cmgfaceMeshId, cmgnode);
            // 创建CMG-LINK
            CmgBuildlink cmglink = CmgLinkOperateUtils.createCmglink(geometry, cmgnode.pid(), cmgnode.pid(), result, false);
            // 创建CMG-LINK子表MESH
            createCmglinkMesh(result, cmgfaceMeshId, cmglink);
            // 创建CMG-FACE
            CmgBuildface cmgface = createCmgface(result, geometry, cmgfaceMeshId);
            // 创建CMG-FACE子表TOPO
            createCmgfaceTopo(result, cmglink.pid(), cmgface.pid(), 1);

        } else {
            // 重新计算线构面LINK顺序
            Map<Integer, Geometry> map = calcCmglinkSequence();
            // 更新参数LINK
            command.setLinkPids(new ArrayList<>(map.keySet()));
            // 连接所有坐标点
            Coordinate[] coordinates = GeoTranslator.getCalLineToPython(new ArrayList<>(map.values())).getCoordinates();
            // 判断是否逆序
            if (!GeometryUtils.IsCCW(coordinates)) {
                List<Coordinate> array = Arrays.asList(coordinates);
                Collections.reverse(array);
                coordinates = array.toArray(new Coordinate[array.size()]);
                Collections.reverse(command.getLinkPids());
            }
            // 通过坐标点构成面
            Geometry geometry = GeoTranslator.getPolygonToPoints(coordinates);
            // 计算CMG-FACE的图幅号
            int cmgfaceMeshId = calcFaceMeshId(geometry);
            // 创建CMG-FACE
            CmgBuildface cmgface = createCmgface(result, geometry, cmgfaceMeshId);
            // 创建CMG-FACE-TOPO
            for (int seq = 1; seq < command.getLinkPids().size(); seq++) {
                createCmgfaceTopo(result, command.getLinkPids().get(seq), cmgface.pid(), seq);
            }
            // 初始化CMG-NODE-SELECTOR
            AbstractSelector cmgnodeSelector = new AbstractSelector(CmgBuildnode.class, conn);
            // 重新计算CMG-LINK-MESH信息、CMG-NODE-MESH信息
            for (IRow row : command.getCmglinks()) {
                CmgBuildlink cmglink = (CmgBuildlink) row;
                // 重新计算CMG-LINK-MESH信息
                calcCmglinkMesh(result, cmgfaceMeshId, cmglink);
                // 重新计算START点的MESH信息
                CmgBuildnode cmgnode = (CmgBuildnode) cmgnodeSelector.loadById(cmglink.getsNodePid(), false);
                calcCmgnodeMesh(result, cmgfaceMeshId, cmglink, cmgnode);
                // 重新计算END点的MESH信息
                cmgnode = (CmgBuildnode) cmgnodeSelector.loadById(cmglink.geteNodePid(), false);
                calcCmgnodeMesh(result, cmgfaceMeshId, cmglink, cmgnode);
            }
        }
        return null;
    }

    /**
     * 创建CMG-NODE-MESH
     * @param result 结果集
     * @param cmgfaceMeshId 关联面的图幅号
     * @param cmgnode CMG-NODE
     */
    private void createCmgnodeMesh(Result result, int cmgfaceMeshId, CmgBuildnode cmgnode) {
        CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
        cmgnodeMesh.setNodePid(cmgnode.pid());
        cmgnodeMesh.setMeshId(cmgfaceMeshId);
        result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmgnode.pid());
    }

    /**
     * 创建CMG-LINK-MESH
     * @param result 结果集
     * @param cmgfaceMeshId 关联面的图幅号
     * @param cmglink CMG-LINK
     */
    private void createCmglinkMesh(Result result, int cmgfaceMeshId, CmgBuildlink cmglink) {
        CmgBuildlinkMesh cmglinkMesh = new CmgBuildlinkMesh();
        cmglinkMesh.setLinkPid(cmglink.pid());
        cmglinkMesh.setMeshId(cmgfaceMeshId);
        result.insertObject(cmglinkMesh, ObjStatus.INSERT, cmglink.pid());
    }

    /**
     * 创建CMG-FACE
     * @param result 结果集
     * @param geometry CMG-FACE几何
     * @param cmgfaceMeshId 根据几何计算出的图幅
     * @return 创建的CMG-FACE对象
     * @throws Exception 创建出错
     */
    private CmgBuildface createCmgface(Result result, Geometry geometry, int cmgfaceMeshId) throws Exception {
        CmgBuildface cmgface = new CmgBuildface();
        cmgface.setPid(PidUtil.getInstance().applyCmgBuildfacePid());
        cmgface.setGeometry(geometry);
        cmgface.setPerimeter(GeometryUtils.getLinkLength(geometry));
        cmgface.setArea(GeometryUtils.getCalculateArea(geometry));
        cmgface.setMeshId(cmgfaceMeshId);
        result.insertObject(cmgface, ObjStatus.INSERT, cmgface.pid());
        return cmgface;
    }

    /**
     * 创建CMG-FACE-TOPO
     * @param result 结果集
     * @param cmglinkPid CMG-LINK主键
     * @param cmgfacePid CMG-FACE主键
     * @param seq 顺序号
     */
    private void createCmgfaceTopo(Result result, int cmglinkPid, int cmgfacePid, int seq) {
        CmgBuildfaceTopo cmgfaceTopo = new CmgBuildfaceTopo();
        cmgfaceTopo.setFacePid(cmgfacePid);
        cmgfaceTopo.setLinkPid(cmglinkPid);
        cmgfaceTopo.setSeqNum(seq);
        result.insertObject(cmgfaceTopo, ObjStatus.INSERT, cmgfacePid);
    }

    /**
     *  重新计算CMG-LINK-MESH, 清除原不匹配图幅，没有新图幅号时重新生成
     * @param result 结果集
     * @param cmgfaceMeshId 关联面的图幅号
     * @param cmglink CMG-LINK
     */
    private void calcCmglinkMesh(Result result, int cmgfaceMeshId, CmgBuildlink cmglink) {
        Iterator<IRow> iterator = cmglink.getMeshes().iterator();
        while (iterator.hasNext()) {
            CmgBuildlinkMesh cmglinkMesh = (CmgBuildlinkMesh) iterator.next();
            if (cmgfaceMeshId != cmglinkMesh.getMeshId()) {
                iterator.remove();
                result.insertObject(cmglinkMesh, ObjStatus.DELETE, cmglink.pid());
            }
        }
        if (CollectionUtils.isEmpty(cmglink.getMeshes())) {
            createCmglinkMesh(result, cmgfaceMeshId, cmglink);
        }
    }

    /**
     *
     * @param result 结果集
     * @param cmgfaceMeshId 关联面的图幅号
     * @param cmglink CMG-LINK
     * @param cmgnode CMG-NODE
     */
    private void calcCmgnodeMesh(Result result, int cmgfaceMeshId, CmgBuildlink cmglink, CmgBuildnode cmgnode) {
        Iterator<IRow> iterator;
        iterator = cmgnode.getMeshes().iterator();
        while (iterator.hasNext()) {
            CmgBuildnodeMesh cmgnodeMesh = (CmgBuildnodeMesh) iterator.next();
            if (cmgfaceMeshId != cmgnodeMesh.getMeshId()) {
                iterator.remove();
                result.insertObject(cmgnodeMesh, ObjStatus.DELETE, cmgnode.pid());
            }
        }
        if (CollectionUtils.isEmpty(cmgnode.getMeshes())) {
            CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
            cmgnodeMesh.setNodePid(cmgnode.pid());
            cmgnodeMesh.setMeshId(cmgfaceMeshId);
            result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmglink.pid());
        }
    }

    /**
     * 对线构面CMG-LINK进行重排序
     * @return Integer-排序后线PID, 对应线的几何
     * @throws Exception 重排序出错
     */
    private Map<Integer, Geometry> calcCmglinkSequence() throws Exception {
        Map<Integer, Geometry> map = new HashMap<>();
        CmgBuildlink firstLink = (CmgBuildlink) command.getCmglinks().get(0);
        map.put(firstLink.pid(), firstLink.getGeometry());
        int firstNodePid = firstLink.getsNodePid();
        int nextNodePid = firstLink.geteNodePid();
        int count = 1;
        while (firstNodePid != nextNodePid) {
            if (count != map.size()) {
                throw new Exception("所选线无法构成闭合面");
            }
            for (IRow row : command.getCmglinks()) {
                CmgBuildlink cmglink = (CmgBuildlink) row;
                if (nextNodePid == cmglink.getsNodePid()) {
                    nextNodePid = cmglink.geteNodePid();
                } else if (nextNodePid == cmglink.geteNodePid()) {
                    nextNodePid = cmglink.getsNodePid();
                } else {
                    continue;
                }
                map.put(cmglink.pid(), cmglink.getGeometry());
            }
            count++;
        }
        return map;
    }

    /**
     * 计算CMG-FACE的图幅号
     * @param geometry 几何信息
     * @return 图幅号
     */
    private int calcFaceMeshId(Geometry geometry) {
        String meshId = "0";

        Point centro = geometry.getCentroid();
        String[] meshes = MeshUtils.point2Meshes(centro.getX(), centro.getY());
        if (meshes.length == 1) {
            meshId = meshes[0];
        } else if (meshes.length == 2) {
            meshId = meshes[1];
        } else if (meshes.length == 4) {
            meshId = meshes[3];
        }
        return Integer.valueOf(meshId);
    }
}
