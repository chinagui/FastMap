package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart.*;
import com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart.Operation;

public class OpRefRelationObj {

    private Connection conn = null;

    public OpRefRelationObj(Connection conn) {
        this.conn = conn;
    }

    // 同一线
    public String handleSameLink(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(conn);
        operation.deleteByUpDownPartLink(command.getsNode().getPid(), command.geteNode().getPid(), command.getLinks(), result);
        return "";
    }

    // 同一点
    public String handleSameNode(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(conn);
        operation.deleteByUpDownPartLink(command.getsNode().getPid(), command.geteNode().getPid(), command.getLinks(), result);
        return "";
    }

    // 警示信息
    public String handlerdWarninginfo(Command command, Result result) throws Exception {

//        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(conn);
//        warninginfoOperation.batchDeleteByLink(command.getLinks(), result);
//        return null;
        // 维护警示信息
        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 点限速
    public String handlerdSpeedlimit(Command command, Result result) throws Exception {
//        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation(conn);
//        warninginfoOperation.upDownLink(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
//        return null;
        // 维护点限速
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护电子眼信息
    public String handlerdRdElectroniceye(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护RTIC信息
    public String handlerRdLinkRtic(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdlink.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护IxPoi信息
    public String handlerIxPoi(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.poi.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护减速带信息
    public String handlerRdSpeedbump(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护坡度信息
    public String handlerRdSlope(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护可变限速
    public String handlerRdVariableSpeed(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护信号灯
    public String handlerRdTrafficsignal(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

    // 维护路口
    public String handlerRdCross(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
    }

}
