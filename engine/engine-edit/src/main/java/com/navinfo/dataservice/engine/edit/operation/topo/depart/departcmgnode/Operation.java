package com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

import java.sql.Connection;

/**
 * @Title: Operation
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode
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
     * 数据链接
     */
    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        if (0 == command.getCatchNodePid() && 0 == command.getCatchLinkPid()) {
            if (1 == command.getCmglinks().size()) {
                this.moveCmgBuildnode(result);
                this.moveCmgBuildlink(result);
            } else {
                CmgBuildnode cmgnode = NodeOperateUtils.createCmgBuildnode(command.getPoint().getX(), command.getPoint().getY());
                result.insertObject(cmgnode, ObjStatus.INSERT, cmgnode.pid());
                this.updateCmgBuildLink(cmgnode, result);
            }
        } else {
            // 节点挂接
            if (0 != command.getCatchNodePid()) {
                CmgBuildnode cmgnode = (CmgBuildnode) new AbstractSelector(CmgBuildnode.class, conn).
                        loadById(command.getCatchNodePid(), false);
                this.updateCmgBuildLink(cmgnode, result);
            }
            // 打断已有线
            if (0 != command.getCatchLinkPid()) {
                CmgBuildnode cmgnode = command.getCmgnode();
                if (1 == command.getCmglinks().size()) {
                    this.moveCmgBuildnode(result);
                    this.moveCmgBuildlink(result);
                } else {
                    cmgnode = NodeOperateUtils.createCmgBuildnode(command.getPoint().getX(), command.getPoint().getY());
                    result.insertObject(cmgnode, ObjStatus.INSERT, cmgnode.pid());
                    this.updateCmgBuildLink(cmgnode, result);
                }
                this.breakCatchCmgBuildlink(result, cmgnode);
            }
        }

        return null;
    }

    /**
     * 打断分离后挂接的CMG-LINK
     * @param result 结果集
     * @param cmgnode 打断点
     * @throws Exception 打断过程出错
     */
    private void breakCatchCmgBuildlink(Result result, CmgBuildnode cmgnode) throws Exception {
        JSONObject breakJson = new JSONObject();
        breakJson.put("objId", command.getCatchLinkPid());
        breakJson.put("dbId", command.getDbId());
        JSONObject data = new JSONObject();
        Geometry cmgnodeGeo = GeoTranslator.transform(cmgnode.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        data.put("longitude", cmgnodeGeo.getCoordinate().x);
        data.put("latitude", cmgnodeGeo.getCoordinate().y);
        data.put("breakNodePid", cmgnode.pid());
        breakJson.put("data", data);
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command breakCommand =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command(breakJson, breakJson.toString());
        com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process breakProcess =
                new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process(breakCommand, result, conn);
        breakProcess.innerRun();
    }

    /**
     * 修改CMG-LINK几何，修改CMG-LINK起始点PID
     * @param cmgnode 生成的CMG-NODE
     * @param result 结果集
     * @throws Exception 修改CMG-LINK出错
     */
    private void updateCmgBuildLink(CmgBuildnode cmgnode, Result result) throws Exception {
        Geometry cmglinkGeo = GeoTranslator.transform(
                command.getCmglink().getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        Geometry cmgnodeGeo = GeoTranslator.transform(
                cmgnode.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);

        if (command.getCmglink().getsNodePid() == command.getCmgnode().pid()) {
            cmglinkGeo.getCoordinates()[0].x = cmgnodeGeo.getCoordinate().x;
            cmglinkGeo.getCoordinates()[0].y = cmgnodeGeo.getCoordinate().y;
                command.getCmglink().changedFields().put("sNodePid", cmgnode.pid());
        } else {
            cmglinkGeo.getCoordinates()[cmglinkGeo.getCoordinates().length - 1].x = cmgnodeGeo.getCoordinate().x;
            cmglinkGeo.getCoordinates()[cmglinkGeo.getCoordinates().length - 1].y = cmgnodeGeo.getCoordinate().y;
                command.getCmglink().changedFields().put("eNodePid", cmgnode.pid());
        }
        command.getCmglink().changedFields().put("length", GeometryUtils.getLinkLength(cmglinkGeo));
        command.getCmglink().changedFields().put("geometry", GeoTranslator.jts2Geojson(cmglinkGeo));
        result.insertObject(command.getCmglink(), ObjStatus.UPDATE, command.getCmglink().pid());
    }

    /**
     * 移动CMG-NODE,
     * @param result 结果集
     * @throws Exception 几何转换出错
     */
    private void moveCmgBuildnode(Result result) throws Exception {
        command.getCmgnode().changedFields().put("geometry", GeoTranslator.jts2Geojson(command.getPoint()));
        result.insertObject(command.getCmgnode(), ObjStatus.UPDATE, command.getCmgnode().pid());
    }

    /**
     * 修形CMG-LINK
     * @param result 结果集
     * @throws org.json.JSONException 修形出错
     */
    private void moveCmgBuildlink(Result result) throws org.json.JSONException {
        Geometry cmglinkGeo = GeoTranslator.transform(
                command.getCmglink().getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
        if (command.getCmglink().getsNodePid() == command.getCmgnode().pid()) {
            cmglinkGeo.getCoordinates()[0].x = command.getPoint().getX();
            cmglinkGeo.getCoordinates()[0].y = command.getPoint().getY();
        } else {
            cmglinkGeo.getCoordinates()[cmglinkGeo.getCoordinates().length - 1].x = command.getPoint().getX();
            cmglinkGeo.getCoordinates()[cmglinkGeo.getCoordinates().length - 1].y = command.getPoint().getY();
        }
        command.getCmglink().changedFields().put("length", GeometryUtils.getLinkLength(cmglinkGeo));
        command.getCmglink().changedFields().put("geometry", GeoTranslator.jts2Geojson(cmglinkGeo));
        result.insertObject(command.getCmglink(), ObjStatus.UPDATE, command.getCmglink().pid());
    }
}
