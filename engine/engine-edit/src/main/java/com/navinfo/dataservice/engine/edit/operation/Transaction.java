package com.navinfo.dataservice.engine.edit.operation;

import java.sql.Connection;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 操作控制器
 */
public class Transaction {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 操作类型
     */
    private OperType operType;

    /**
     * 对象类型
     */
    private ObjType objType;

    /**
     * 命令对象
     */
    private AbstractCommand command;

    /**
     * 操作进程对象
     */
    private AbstractProcess<AbstractCommand> process;

    private Connection conn;

    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public OperType getOperType() {
        return operType;
    }

    public Transaction(String requester) {
        this.requester = requester;
    }

    public Transaction(String requester, Connection conn) {
        this.requester = requester;

        this.conn = conn;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    /**
     * 创建操作命令
     *
     * @return 命令
     */
    private AbstractCommand createCommand() throws Exception {

        // 修改net.sf.JSONObject的bug：string转json对象损失精度问题（解决方案目前有两种，一种替换新的jar包以及依赖的包，第二种先转fastjson后再转net.sf）
        com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject
                .parseObject(requester);

        JSONObject json = JsonUtils.fastJson2netJson(fastJson);

        operType = Enum.valueOf(OperType.class, json.getString("command"));

        objType = Enum.valueOf(ObjType.class, json.getString("type"));

        switch (objType) {
            case RDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode.Command(
                                json, requester);
                    case UPDOWNDEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Command(
                                json, requester);
                    case CREATESIDEROAD:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create.Command(
                                json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink.Command(json, requester);
                }
            case FACE:
                switch (operType) {
                    case ONLINEBATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.rdlink.Command(
                                json, requester);
                }
            case RDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(
                                json, requester);
                }
            case RDRESTRICTION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Command(
                                json, requester);
                }
            case RDCROSS:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.Command(
                                json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross.Command(json, requester);
                }
            case RDBRANCH:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Command(
                                json, requester);
                }
            case RDLANECONNEXITY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Command(
                                json, requester);
                }
            case RDSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Command(
                                json, requester);
                }
            case RDLINKSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Command(
                                json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit.Command(
                                json, requester);
                    default:
                        break;
                }
            case ADADMIN:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Command(
                                json, requester);
                }
            case RDGSC:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Command(
                                json, requester);
                }
            case ADNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Command(
                                json, requester);
                }
            case ADLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode.Command(
                                json, requester);
                }
            case ADFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Command(
                                json, requester);
                }
            case ADADMINGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Command(
                                json, requester);
                }
            case IXPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Command(
                                json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.poi.Command(
                                json, requester);
                }
            case IXPOIPARENT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Command(
                                json, requester);

                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Command(
                                json, requester);

                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Command(
                                json);
                }
            case RWNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command(
                                json, requester);
                }

            case RWLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode.Command(
                                json, requester);
                }
            case ZONENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Command(
                                json, requester);
                }
            case ZONELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode.Command(
                                json, requester);
                }
            case ZONEFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Command(
                                json, requester);
                }
            case LUNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Command(
                                json, requester);
                }
            case LULINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode.Command(
                                json, requester);
                }
            case LUFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Command(
                                json, requester);
                }
            case LCNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode.Command(
                                json, requester);
                }
            case LCLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.update.Command(
                                json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink.Command(
                                json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink.Command(
                                json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode.Command(
                                json, requester);
                }
            case LCFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.delete.Command(
                                json, requester);
                }
            case RDELECTRONICEYE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Command(
                                json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Command(
                                json, requester);
                }
            case RDELECEYEPAIR:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDTRAFFICSIGNAL:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Command(
                                json, requester);
                    default:
                        break;
                }

            case RDWARNINGINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDSLOPE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Command(
                                json, requester);
                    default:
                        break;
                }

            case RDDIRECTROUTE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDINTER:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDOBJECT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDVARIABLESPEED:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDSE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDSPEEDBUMP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDSAMENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDTOLLGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDVOICEGUIDE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Command(
                                json, requester);
                    default:
                        break;
                }

            case RDROAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Command(
                                json, requester);
                    default:
                        break;
                }

            case RDSAMELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDLANE:
                switch (operType) {
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete.Command(
                                json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDLANETOPODETAIL:
                switch (operType) {
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail.Command(
                                json, requester);
                    default:
                        break;
                }
            case IXSAMEPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case IXPOIUPLOAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.create.Command(
                                json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(
                                json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.delete.Command(
                                json, requester);
                    default:
                        break;
                }
            case RDHGWGLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Command(json, requester);
                    default:
                        break;
                }
            case RDMILEAGEPILE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Command(json, requester);
                    default:
                        break;
                }
            case RDTMCLOCATION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.delete.Command(json, requester);
                    default:
                        break;
                }
        }

        throw new Exception("不支持的操作类型");
    }

    /**
     * 创建操作进程
     *
     * @param command 操作命令
     * @return 操作进程
     * @throws Exception
     */
    private AbstractProcess createProcess(AbstractCommand command)
            throws Exception {

        switch (objType) {
            case RDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
                                command);

                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Process(
                                command);
                    case UPDOWNDEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Process(
                                command);
                    case CREATESIDEROAD:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create.Process(
                                command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink.Process(command);
                }
            case FACE:
                switch (operType) {
                    case ONLINEBATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.rdlink.Process(
                                command);
                }
            case RDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(
                                command);
                }
            case RDRESTRICTION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Process(
                                command);
                }
            case RDCROSS:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.Process(
                                command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross.Process(command);
                }
            case RDBRANCH:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Process(
                                command);
                }
            case RDLANECONNEXITY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Process(
                                command);
                }
            case RDSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Process(
                                command);
                }
            case RDLINKSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Process(
                                command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit.Process(
                                command);
                    default:
                        break;
                }
            case ADADMIN:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Process(
                                command);
                }
            case RDGSC:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Process(
                                command);
                }
            case ADNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Process(
                                command);
                }
            case ADLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(
                                command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode.Process(
                                command);

                }
            case ADFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Process(
                                command);
                }
            case ADADMINGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Process(
                                command);
                }
            case IXPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Process(
                                command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.poi.Process(
                                command);
                }
            case IXPOIPARENT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Process(
                                command);

                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Process(
                                command);

                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Process(
                                command);
                }
            case RWNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process(
                                command);
                }
            case RWLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(
                                command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode.Process(
                                command);
                }

            case ZONENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Process(
                                command);
                }
            case ZONELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(
                                command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode.Process(
                                command);
                }
            case ZONEFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Process(
                                command);
                }
            case LUNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Process(
                                command);
                }
            case LULINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(
                                command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode.Process(
                                command);
                }
            case LUFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Process(
                                command);
                }
            case RDELECTRONICEYE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Process(
                                command);
                }
            case RDELECEYEPAIR:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Process(
                                command);
                    default:
                        break;
                }
            case RDTRAFFICSIGNAL:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Process(
                                command);
                    default:
                        break;
                }

            case RDWARNINGINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Process(
                                command);
                    default:
                        break;
                }

            case RDSLOPE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Process(
                                command);
                    default:
                        break;
                }

            case RDGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Process(
                                command);
                    default:
                        break;
                }

            case RDDIRECTROUTE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Process(
                                command);
                    default:
                        break;
                }
            case RDINTER:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Process(
                                command);
                    default:
                        break;
                }
            case RDOBJECT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Process(
                                command);
                    default:
                        break;
                }
            case RDVARIABLESPEED:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Process(
                                command);
                    default:
                        break;
                }
            case RDSE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Process(
                                command);
                    default:
                        break;
                }
            case RDSPEEDBUMP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Process(
                                command);
                    default:
                        break;
                }
            case LCNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process(
                                command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode.Process(
                                command);
                }
            case LCLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.update.Process(
                                command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink.Process(
                                command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink.Process(
                                command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode.Process(
                                command);
                }
            case LCFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.delete.Process(
                                command);
                }
            case RDSAMENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Process(
                                command);
                    default:
                        break;
                }
            case RDTOLLGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Process(
                                command);
                    default:
                        break;
                }

            case RDVOICEGUIDE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Process(
                                command);
                    default:
                        break;
                }
            case RDROAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Process(
                                command);
                    default:
                        break;
                }

            case RDSAMELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Process(
                                command);
                    default:
                        break;
                }
            case RDLANE:
                switch (operType) {
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete.Process(
                                command);

                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Process(
                                command);
                    default:
                        break;
                }
            case RDLANETOPODETAIL:
                switch (operType) {
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail.Process(
                                command);
                    default:
                        break;
                }
            case IXSAMEPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.delete.Process(
                                command);
                    default:
                        break;
                }
            case IXPOIUPLOAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.create.Process(
                                command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(
                                command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.delete.Process(
                                command);
                    default:
                        break;
                }
            case RDHGWGLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Process(command);
                    default:
                        break;
                }
            case RDMILEAGEPILE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.move.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Process(command);
                    default:
                        break;
                }
            case RDTMCLOCATION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.delete.Process(command);
                    default:
                        break;
                }
        }

        throw new Exception("不支持的操作类型");

    }

    public Result createResult() {
        return null;
    }

    /**
     * 执行操作
     *
     * @return
     * @throws Exception
     */
    public String run() throws Exception {
        command = this.createCommand();
        command.setUserId(userId);

        process = this.createProcess(command);

        return process.run();

    }

    /**
     * 执行操作
     *
     * @return
     * @throws Exception
     */
    public String innerRun() throws Exception {
        command = this.createCommand();
        command.setUserId(userId);
        if (conn != null) {
            command.setHasConn(true);
        }
        process = this.createProcess(command);
        process.setConn(conn);

        return process.innerRun();

    }

    /**
     * @return 操作简要日志信息
     */
    public String getLogs() {

        return process.getResult().getLogs();
    }

    public JSONArray getCheckLog() {
        return process.getResult().getCheckResults();

    }

    public int getPid() {
        return process.getResult().getPrimaryPid();
    }

    public static void main(String[] args) {
        String requester = "{\"data\":{\"infos\":\"[1],[2],[3]\"}}";
        JSONObject json = JSONObject.fromObject(requester,
                JsonUtils.getStrConfig());
        System.out.println(json);
    }
}
