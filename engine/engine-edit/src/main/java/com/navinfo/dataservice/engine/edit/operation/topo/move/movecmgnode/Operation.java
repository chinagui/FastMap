package com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlinkMesh;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnodeMesh;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.CmgfaceUtil;
import com.navinfo.dataservice.engine.edit.utils.CmgLinkOperateUtils;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: Opeartion
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
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

    @Override
    public String run(Result result) throws Exception {
        Map<Integer, List<Integer>> linkOfMeshId = new HashMap<>();
        Map<Integer, Integer> faceMeshIds = new HashMap<>();
        // 处理CMG-FACE
        for (CmgBuildface cmgface : command.getCmgfaces()) {
            Geometry geometry = GeoTranslator.transform(cmgface.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
            Geometry cmgnodeGeo = GeoTranslator.transform(command.getCmgnode().getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
            for (Coordinate coordinate : geometry.getCoordinates()) {
                if (cmgnodeGeo.getCoordinate().x == coordinate.x && cmgnodeGeo.getCoordinate().y == coordinate.y) {
                    coordinate.x = command.getLongitude();
                    coordinate.y = command.getLatitude();
                }
            }
            if (MeshUtils.mesh2Jts(String.valueOf(cmgface.getMeshId())).intersects(geometry)) {
                int cmgfaceMeshId = CmgfaceUtil.calcFaceMeshId(geometry.getCentroid());
                cmgface.changedFields().put("meshId", cmgfaceMeshId);
                faceMeshIds.put(cmgface.getMeshId(), cmgfaceMeshId);
                for (IRow row : cmgface.getTopos()) {
                    CmgBuildfaceTopo cmgfaceTopo = (CmgBuildfaceTopo) row;
                    linkOfMeshId.put(cmgfaceTopo.getLinkPid(), Arrays.asList(cmgface.getMeshId(), cmgfaceMeshId));
                }
            }
            cmgface.changedFields().put("geometry", GeoTranslator.jts2Geojson(geometry));
            cmgface.changedFields().put("perimeter", GeometryUtils.getLinkLength(geometry));
            cmgface.changedFields().put("area", GeometryUtils.getCalculateArea(geometry));
            result.insertObject(cmgface, ObjStatus.UPDATE, cmgface.pid());
        }
        // 处理CMG-LINK
        for (CmgBuildlink cmglink : command.getCmglinks()) {
            Geometry geometry = GeoTranslator.transform(cmglink.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
            if (command.getCmgnode().pid() == cmglink.getsNodePid()) {
                geometry.getCoordinates()[0].x = command.getLongitude();
                geometry.getCoordinates()[0].y = command.getLatitude();
            }
            if (command.getCmgnode().pid() == cmglink.geteNodePid()) {
                geometry.getCoordinates()[geometry.getCoordinates().length - 1].x = command.getLongitude();
                geometry.getCoordinates()[geometry.getCoordinates().length - 1].y = command.getLatitude();
            }
            cmglink.changedFields().put("length", GeometryUtils.getLinkLength(geometry));
            // 验证LINK长度
            CmgLinkOperateUtils.validateLength(geometry);
            cmglink.changedFields().put("geometry", GeoTranslator.jts2Geojson(geometry));
            result.insertObject(cmglink, ObjStatus.UPDATE, cmglink.pid());

            if (linkOfMeshId.containsKey(cmglink.pid())) {
                List<Integer> linkMeshes = linkOfMeshId.get(cmglink.pid());
                for (IRow row : cmglink.getMeshes()) {
                    CmgBuildlinkMesh cmglinkMesh = (CmgBuildlinkMesh) row;
                    if (linkMeshes.get(0) == cmglinkMesh.getMeshId()) {
                        cmglinkMesh.changedFields.put("meshId", linkMeshes.get(1));
                        result.insertObject(cmglinkMesh, ObjStatus.UPDATE, cmglink.pid());
                    }
                }
            }
        }
        // 处理CMG-NODE
        Geometry geometry = new GeometryFactory().createPoint(
                new Coordinate(command.getLongitude(), command.getLatitude()));
        command.getCmgnode().changedFields().put("geometry", GeoTranslator.jts2Geojson(geometry));
        result.insertObject(command.getCmgnode(), ObjStatus.UPDATE, command.getCmgnode().pid());
        if (!CollectionUtils.isEmpty(faceMeshIds)) {
            for (IRow row : command.getCmgnode().getMeshes()) {
                CmgBuildnodeMesh cmgnodeMesh = (CmgBuildnodeMesh) row;
                if (faceMeshIds.values().contains(cmgnodeMesh.getMeshId())) {
                    faceMeshIds.remove(cmgnodeMesh.getMeshId());
                } else {
                    result.insertObject(cmgnodeMesh, ObjStatus.DELETE, cmgnodeMesh.parentPKValue());
                }
            }
            for (Integer meshId : faceMeshIds.values()) {
                CmgBuildnodeMesh cmgnodeMesh = new CmgBuildnodeMesh();
                cmgnodeMesh.setNodePid(command.getCmgnode().pid());
                cmgnodeMesh.setMeshId(meshId);
                result.insertObject(cmgnodeMesh, ObjStatus.INSERT, cmgnodeMesh.parentPKValue());
            }
        }
        return null;
    }
}
