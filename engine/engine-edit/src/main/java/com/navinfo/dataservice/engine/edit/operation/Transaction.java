package com.navinfo.dataservice.engine.edit.operation;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.dao.glm.model.rd.crf.*;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.operation.edge.EdgeOperation;
import com.navinfo.dataservice.engine.edit.operation.edge.EdgeUtil;
import com.navinfo.dataservice.engine.edit.operation.edge.TransactionFactory;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.GeometryUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.analysis.function.Abs;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.sql.Connection;
import java.util.*;

/**
 * 操作控制器
 */
public class Transaction {

    private static Logger logger = Logger.getLogger(Transaction.class);

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
     * 数据库链接
     */
    private Connection conn;

    /**
     * 用户Id
     */
    private long userId;

    /**
     * 子任务Id
     */
    private int subTaskId;

    /**
     * 数据库类型
     */
    private int dbType;

    /**
     * 主要操作
     */
    private AbstractProcess process;

    /**
     * 删除标识
     * 1：提示，0：删除
     */
    private int infect = 0;

    public Transaction(String requester) {
        this.requester = requester;
    }

    public Transaction(String requester, Connection conn) {
        this.requester = requester;
        this.conn = conn;
    }

    /**
     * Getter method for property <tt>requester</tt>.
     *
     * @return property value of requester
     */
    public String getRequester() {
        return requester;
    }

    /**
     * Setter method for property <tt>requester</tt>.
     *
     * @param requester value to be assigned to property requester
     */
    public void setRequester(String requester) {
        this.requester = requester;
    }

    /**
     * Getter method for property <tt>operType</tt>.
     *
     * @return property value of operType
     */
    public OperType getOperType() {
        return operType;
    }

    /**
     * Getter method for property <tt>objType</tt>.
     *
     * @return property value of objType
     */
    public ObjType getObjType() {
        return objType;
    }

    /**
     * Setter method for property <tt>objType</tt>.
     *
     * @param objType value to be assigned to property objType
     */
    public void setObjType(ObjType objType) {
        this.objType = objType;
    }

    /**
     * Getter method for property <tt>conn</tt>.
     *
     * @return property value of conn
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Setter method for property <tt>conn</tt>.
     *
     * @param conn value to be assigned to property conn
     */
    public void setConn(Connection conn) {
        this.conn = conn;
    }

    /**
     * Getter method for property <tt>userId</tt>.
     *
     * @return property value of userId
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Setter method for property <tt>userId</tt>.
     *
     * @param userId value to be assigned to property userId
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * Getter method for property <tt>subTaskId</tt>.
     *
     * @return property value of subTaskId
     */
    public int getSubTaskId() {
        return subTaskId;
    }

    /**
     * Setter method for property <tt>subTaskId</tt>.
     *
     * @param subTaskId value to be assigned to property subTaskId
     */
    public void setSubTaskId(int subTaskId) {
        this.subTaskId = subTaskId;
    }

    /**
     * Setter method for property <tt>dbType</tt>.
     *
     * @param dbType value to be assigned to property dbType
     */
    public void setDbType(int dbType) {
        this.dbType = dbType;
    }

    /**
     * Getter method for property <tt>infect</tt>.
     *
     * @return property value of infect
     */
    public int getInfect() {
        return infect;
    }

    /**
     * Setter method for property <tt>infect</tt>.
     *
     * @param infect value to be assigned to property infect
     */
    public void setInfect(int infect) {
        this.infect = infect;
    }

    /**
     * 创建操作命令
     *
     * @return 命令
     */
    public AbstractCommand createCommand(String requester) throws Exception {
        // 修改net.sf.JSONObject的bug：string转json对象损失精度问题（解决方案目前有两种，一种替换新的jar包以及依赖的包，第二种先转fastjson后再转net.sf）
        com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject.parseObject(requester);
        JSONObject json = JsonUtils.fastJson2netJson(fastJson);

        operType = Enum.valueOf(OperType.class, json.getString("command"));
        objType = Enum.valueOf(ObjType.class, json.getString("type"));
        if (json.containsKey("infect")) {
            infect = json.getInt("infect");
        }

        switch (objType) {
            case RDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode.Command(json, requester);
                    case UPDOWNDEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Command(json, requester);
                    case CREATESIDEROAD:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create.Command(json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink.Command(json, requester);
                    case BATCHDELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdlink.Command(json, requester);
                    case TOPOBREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.topobreakin.Command(json, requester);
                }
            case FACE:
                switch (operType) {
                    case ONLINEBATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.rdlink.Command(json, requester);
                }
            case RDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdnode.Command(json, requester);
                    case BATCHDELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdnode.Command(json, requester);
                }
            case RDRESTRICTION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Command(json, requester);
                }
            case RDCROSS:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.Command(json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross.Command(json, requester);
                }
            case RDBRANCH:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Command(json, requester);
                }
            case RDLANECONNEXITY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Command(json, requester);
                }
            case RDSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Command(json, requester);
                }
            case RDLINKSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Command(json,
                                requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit.Command(json, requester);
                }
            case ADADMIN:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Command(json, requester);
                    case RELATION:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.relation.Command(json, requester);
                }
            case RDGSC:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Command(json, requester);
                }
            case ADNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Command(json, requester);
                }
            case ADLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode.Command(json, requester);
                }
            case ADFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Command(json, requester);
                }
            case ADADMINGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Command(json, requester);
                }
            case IXPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Command(json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.poi.Command(json, requester);
                    case BATCHMOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.batchmove.Command(json, requester);
                }
            case IXPOIPARENT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Command(json, requester);
                }
            case RWNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command(json, requester);
                }
            case RWLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode.Command(json, requester);
                }
            case ZONENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Command(json, requester);
                }
            case ZONELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode.Command(json, requester);
                }
            case ZONEFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Command(json, requester);
                }
            case LUNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Command(json, requester);
                }
            case LULINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode.Command(json, requester);
                }
            case LUFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Command(json, requester);
                }
            case LCNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode.Command(json, requester);
                }
            case LCLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.update.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode.Command(json, requester);
                }
            case LCFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.delete.Command(json, requester);
                }
            case RDELECTRONICEYE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Command(json, requester);
                }
            case RDELECEYEPAIR:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Command(json, requester);
                }
            case RDTRAFFICSIGNAL:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Command(json, requester);
                }
            case RDWARNINGINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Command(json, requester);
                }
            case RDLINKWARNING:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Command(json, requester);
                }
            case RDSLOPE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Command(json, requester);
                }
            case RDGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Command(json, requester);
                }

            case RDDIRECTROUTE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Command(json, requester);
                }
            case RDINTER:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Command(json, requester);
                }
            case RDOBJECT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Command(json, requester);
                }
            case RDVARIABLESPEED:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Command(json, requester);
                }
            case RDSE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Command(json, requester);
                }
            case RDSPEEDBUMP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Command(json, requester);
                }
            case RDSAMENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Command(json, requester);
                }
            case RDTOLLGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Command(json, requester);
                }
            case RDVOICEGUIDE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Command(json, requester);
                }
            case RDROAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Command(json, requester);
                }
            case RDSAMELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Command(json, requester);
                }
            case RDLANE:
                switch (operType) {
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete.Command(json, requester);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Command(json, requester);
                }
            case RDLANETOPODETAIL:
                switch (operType) {
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail.Command(json, requester);
                }
            case IXSAMEPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.delete.Command(json, requester);
                }
            case IXPOIUPLOAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.delete.Command(json, requester);
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
                }
            case RDTMCLOCATION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.delete.Command(json, requester);
                }
            case CMGBUILDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.update.Command(json, requester);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode.Command(json, requester);
                }
            case CMGBUILDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.update.Command(json, requester);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink.Command(json, requester);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink.Command(json, requester);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode.Command(json, requester);
                }
            case CMGBUILDFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete.Command(json, requester);
                }
            case CMGBUILDING:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create.Command(json, requester);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.update.Command(json, requester);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.delete.Command(json, requester);
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
    public AbstractProcess createProcess(AbstractCommand command) throws Exception {
        switch (objType) {
            case RDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Process(command);
                    case UPDOWNDEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Process(command);
                    case CREATESIDEROAD:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink.Process(command);
                    case BATCHDELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdlink.Process(command);
                    case TOPOBREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.topobreakin.Process(command);
                }
            case FACE:
                switch (operType) {
                    case ONLINEBATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.rdlink.Process(command);
                }
            case RDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdnode.Process(command);
                    case BATCHDELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdnode.Process(command);
                }
            case RDRESTRICTION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Process(command);
                }
            case RDCROSS:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross.Process(command);
                }
            case RDBRANCH:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Process(command);
                }
            case RDLANECONNEXITY:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Process(command);
                }
            case RDSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Process(command);
                }
            case RDLINKSPEEDLIMIT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit.Process(command);
                }
            case ADADMIN:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Process(command);
                    case RELATION:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.relation.Process(command);
                }
            case RDGSC:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Process(command);
                }
            case ADNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Process(command);
                }
            case ADLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode.Process(command);
                }
            case ADFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Process(command);
                }
            case ADADMINGROUP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Process(command);
                }
            case IXPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.batch.poi.Process(command);
                    case BATCHMOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.batchmove.Process(command);
                }
            case IXPOIPARENT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Process(command);
                }
            case RWNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process(command);
                }
            case RWLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode.Process(command);
                }

            case ZONENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Process(command);
                }
            case ZONELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode.Process(command);
                }
            case ZONEFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Process(command);
                }
            case LUNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Process(command);
                }
            case LULINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode.Process(command);
                }
            case LUFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Process(command);
                }
            case RDELECTRONICEYE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move.Process(command);
                }
            case RDELECEYEPAIR:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Process(command);
                }
            case RDTRAFFICSIGNAL:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Process(command);
                }
            case RDWARNINGINFO:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Process(command);
                }
            case RDLINKWARNING:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Process(command);
                }
            case RDSLOPE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Process(command);
                }
            case RDGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Process(command);
                }
            case RDDIRECTROUTE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Process(command);
                }
            case RDINTER:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update.Process(command);
                }
            case RDOBJECT:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Process(command);
                }
            case RDVARIABLESPEED:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Process(command);
                }
            case RDSE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Process(command);
                }
            case RDSPEEDBUMP:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Process(command);
                }
            case LCNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode.Process(command);
                }
            case LCLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lclink.update.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode.Process(command);
                }
            case LCFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.lcface.delete.Process(command);
                }
            case RDSAMENODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Process(command);
                }
            case RDTOLLGATE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Process(command);
                }
            case RDVOICEGUIDE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Process(command);
                }
            case RDROAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete.Process(command);
                }
            case RDSAMELINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Process(command);
                }
            case RDLANE:
                switch (operType) {
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete.Process(command);
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Process(command);
                }
            case RDLANETOPODETAIL:
                switch (operType) {
                    case BATCH:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail.Process(command);
                }
            case IXSAMEPOI:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.samepoi.delete.Process(command);
                }
            case IXPOIUPLOAD:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.delete.Process(command);
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
                }
            case RDTMCLOCATION:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.tmc.delete.Process(command);
                }
            case CMGBUILDNODE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.update.Process(command);
                    case MOVE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode.Process(command);
                }
            case CMGBUILDLINK:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.update.Process(command);
                    case BREAK:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink.Process(command);
                    case REPAIR:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink.Process(command);
                    case DEPART:
                        return new com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode.Process(command);
                }
            case CMGBUILDFACE:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete.Process(command);
                }
            case CMGBUILDING:
                switch (operType) {
                    case CREATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create.Process(command);
                    case UPDATE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.update.Process(command);
                    case DELETE:
                        return new com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.delete.Process(command);
                }
        }
        throw new Exception("不支持的操作类型");

    }

    /**
     * 根据结果集计算是否为跨大区作业
     *
     * @param result
     * @throws Exception
     */
    private Map<Integer, Result> calcDbIdsRefResult(Result result, Integer sourceDbId) throws Exception {
        Map<Integer, Result> resultMap = new HashMap<>();
        Map<Integer, List<IRow>> additional = new HashMap<>();
        // 计算修改数据
        Map<Integer, Map<IRow, ObjStatus>> updateData = calcDbIdRefUpdateList(result.getUpdateObjects(), sourceDbId);
        for (Map.Entry<Integer, Map<IRow, ObjStatus>> entry : updateData.entrySet()) {
            if (entry.getKey().equals(sourceDbId)) {
                resultMap.put(entry.getKey(), null);
                continue;
            }

            if (resultMap.containsKey(entry.getKey())) {
                for (Map.Entry<IRow, ObjStatus> subEntry: entry.getValue().entrySet()) {
                    if (subEntry.getValue().equals(ObjStatus.INSERT)) {
                        resultMap.get(entry.getKey()).getAddObjects().add(subEntry.getKey());
                        resultMap.get(entry.getKey()).getListAddIRowObPid().add(subEntry.getKey().parentPKValue());
                        if (additional.containsKey(entry.getKey())) {
                            additional.get(entry.getKey()).add(subEntry.getKey());
                        } else {
                            List<IRow> rows = new ArrayList<>();
                            rows.add(subEntry.getKey());
                            additional.put(entry.getKey(), rows);
                        }
                    } else {
                        resultMap.get(entry.getKey()).getUpdateObjects().add(subEntry.getKey());
                        resultMap.get(entry.getKey()).getListUpdateIRowObPid().add(subEntry.getKey().parentPKValue());
                    }
                }
            } else {
                Result res = new Result();
                for (Map.Entry<IRow, ObjStatus> subEntry: entry.getValue().entrySet()) {
                    if (subEntry.getValue().equals(ObjStatus.INSERT)) {
                        res.getAddObjects().add(subEntry.getKey());
                        res.getListAddIRowObPid().add(subEntry.getKey().parentPKValue());
                        if (additional.containsKey(entry.getKey())) {
                            additional.get(entry.getKey()).add(subEntry.getKey());
                        } else {
                            List<IRow> rows = new ArrayList<>();
                            rows.add(subEntry.getKey());
                            additional.put(entry.getKey(), rows);
                        }

                    } else {
                        res.getUpdateObjects().add(subEntry.getKey());
                        res.getListUpdateIRowObPid().add(subEntry.getKey().parentPKValue());
                    }
                }
                resultMap.put(entry.getKey(), res);
            }
        }
        // 计算新增数据
        Map<Integer, List<IRow>> createData = calcDbIdRefAddOrDelList(result.getAddObjects(), sourceDbId, additional);
        for (Map.Entry<Integer, List<IRow>> entry : createData.entrySet()) {
            if (entry.getKey().equals(sourceDbId)) {
                resultMap.put(entry.getKey(), null);
                continue;
            }

            if (resultMap.containsKey(entry.getKey())) {
                for (IRow row : entry.getValue()) {
                    resultMap.get(entry.getKey()).getAddObjects().add(row);
                    resultMap.get(entry.getKey()).getListAddIRowObPid().add(row.parentPKValue());
                }
            } else {
                Result res = new Result();
                for (IRow row : entry.getValue()) {
                    res.getAddObjects().add(row);
                    res.getListAddIRowObPid().add(row.parentPKValue());
                }
                resultMap.put(entry.getKey(), res);
            }
        }
        // 计算删除数据
        Map<Integer, List<IRow>> deleteData = calcDbIdRefAddOrDelList(result.getDelObjects(), sourceDbId, new HashMap<Integer, List<IRow>>());
        for (Map.Entry<Integer, List<IRow>> entry : deleteData.entrySet()) {
            if (entry.getKey().equals(sourceDbId)) {
                resultMap.put(entry.getKey(), null);
                continue;
            }

            if (resultMap.containsKey(entry.getKey())) {
                for (IRow row : entry.getValue()) {
                    resultMap.get(entry.getKey()).getDelObjects().add(row);
                    resultMap.get(entry.getKey()).getListDelIRowObPid().add(row.parentPKValue());
                }
            } else {
                Result res = new Result();
                for (IRow row : entry.getValue()) {
                    res.getDelObjects().add(row);
                    res.getListDelIRowObPid().add(row.parentPKValue());
                }
                resultMap.put(entry.getKey(), res);
            }
        }

        logger.info(String.format("本次操作涉及数据库ID:[%s]", StringUtils.join(resultMap.keySet(), ",")));

        return resultMap;
    }

    /**
     * 计算一组数据对应的大区库
     *
     * @param rows
     * @param sourceDbId
     */
    private Map<Integer, List<IRow>> calcDbIdRefAddOrDelList(List<IRow> rows, Integer sourceDbId, Map<Integer, List<IRow>> additional) {
        Map<Integer, List<IRow>> map = new HashMap<>();
        for (Map.Entry<Integer, List<IRow>> entry : additional.entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // 处理跨大区数据
        ListIterator<IRow> iterator = rows.listIterator();
        while (iterator.hasNext()) {
            IRow row = iterator.next();
            if (row instanceof IObj) {
                Geometry geometry = GeometryUtils.loadGeometry(row);
                if (row.changedFields().containsKey("geometry")) {
                    try {
                        geometry = GeoTranslator.geojson2Jts((JSONObject) row.changedFields().get("geometry"));
                    } catch (JSONException e) {
                        logger.error(String.format("获取更新后几何出错[row.table_name: %s, row.row_id: %s]", row.tableName(), row.rowId()), e);
                    }
                }

                Set<Integer> row2DbIds = DbMeshInfoUtil.calcDbIds(geometry);
                for (Integer dbId : row2DbIds) {
                    if (dbId.equals(sourceDbId)) {
                        continue;
                    }
                    if (map.containsKey(dbId)) {
                        map.get(dbId).add(row);
                    } else {
                        List<IRow> tempRows = new ArrayList<>();
                        tempRows.add(row);
                        map.put(dbId, tempRows);
                    }
                }
            }
        }

        while (iterator.hasPrevious()) {
            IRow row = iterator.previous();

            Integer parentPid = row.parentPKValue();
            String parentTableName = row.parentTableName();

            for (List<IRow> list : map.values()) {
                List<IRow> tempList = new ArrayList<>();
                Iterator<IRow> rowIterator = list.iterator();
                while (rowIterator.hasNext()) {
                    IRow tempRow = rowIterator.next();

                    if (!row.equals(tempRow) && parentPid.equals(tempRow.parentPKValue()) && parentTableName.equals(tempRow.tableName())) {
                        tempList.add(row);
                    }
                }
                list.addAll(tempList);
            }
        }

        Set<Integer> crfDbIds = new HashSet<>();
        // 计算CRF相关信息
        for (Geometry geometry : getCRFGeom(process.getConn(), rows)) {
            crfDbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }
        for (Integer crfDbId : crfDbIds) {
            if (crfDbId.equals(sourceDbId)) {
                map.put(sourceDbId, null);
                continue;
            }

            List<IRow> result;
            if (map.containsKey(crfDbId)) {
                result = map.get(crfDbId);
            } else {
                result = new ArrayList<>();
                map.put(crfDbId, result);
            }

            for (IRow row : rows) {
                if (row instanceof RdInter || row instanceof RdInterLink || row instanceof RdInterNode || row instanceof RdRoad ||
                        row instanceof RdRoadLink || row instanceof RdObject || row instanceof RdObjectInter || row instanceof RdObjectRoad
                        || row instanceof RdObjectNode || row instanceof RdObjectName || row instanceof RdObjectLink) {
                    result.add(row);
                }
            }
        }

        for (Map.Entry<Integer, List<IRow>> entry : additional.entrySet()) {
            map.get(entry.getKey()).removeAll(entry.getValue());
        }

        return map;
    }

    /**
     * 计算一组数据对应的大区库
     *
     * @param rows
     * @param sourceDbId
     */
    private Map<Integer, Map<IRow, ObjStatus>> calcDbIdRefUpdateList(List<IRow> rows,  Integer sourceDbId) {
        Map<Integer, Map<IRow, ObjStatus>> map = new HashMap<>();
        ObjStatus defaultStatus = ObjStatus.UPDATE;

        // 处理跨大区数据
        ListIterator<IRow> iterator = rows.listIterator();
        while (iterator.hasNext()) {
            IRow row = iterator.next();
            if (row instanceof IObj) {
                Geometry geometry = GeometryUtils.loadGeometry(row);
                Map<Integer, ObjStatus> dbMap = new HashMap<>();
                Set<Integer> row2DbIds = DbMeshInfoUtil.calcDbIds(geometry);
                for (Integer dbId : row2DbIds) {
                    dbMap.put(dbId, defaultStatus);
                }

                if (row.changedFields().containsKey("geometry")) {
                    try {
                        geometry = GeoTranslator.geojson2Jts((JSONObject) row.changedFields().get("geometry"));
                        row2DbIds = DbMeshInfoUtil.calcDbIds(geometry);
                        for (Integer dbId : row2DbIds) {
                            if (!dbMap.containsKey(dbId)) {
                                dbMap.put(dbId, ObjStatus.INSERT);
                            }
                        }
                    } catch (JSONException e) {
                        logger.error(String.format("获取更新后几何出错[row.table_name: %s, row.row_id: %s]", row.tableName(), row.rowId()), e);
                    }
                }

                for (Map.Entry<Integer, ObjStatus> entry : dbMap.entrySet()) {
                    if (entry.getKey().equals(sourceDbId)) {
                        continue;
                    }
                    if (ObjStatus.INSERT.equals(entry.getValue())) {
                        row = NodeOperateUtils.clone(row);
                    }

                    if (map.containsKey(entry.getKey())) {
                        map.get(entry.getKey()).put(row, entry.getValue());
                    } else {
                        Map<IRow, ObjStatus> tempRows = new HashMap<>();
                        tempRows.put(row, entry.getValue());
                        map.put(entry.getKey(), tempRows);
                    }
                }
            }
        }

        while (iterator.hasPrevious()) {
            IRow row = iterator.previous();

            Integer parentPid = row.parentPKValue();
            String parentTableName = row.parentTableName();

            for (Map<IRow, ObjStatus> innerMap : map.values()) {
                Iterator<Map.Entry<IRow, ObjStatus>> rowIterator = innerMap.entrySet().iterator();
                while (rowIterator.hasNext()) {
                    Map.Entry<IRow, ObjStatus> tempRow = rowIterator.next();

                    if ((!row.equals(tempRow) && !row.objType().equals(tempRow.getKey().objType()))
                            && parentPid.equals(tempRow.getKey().parentPKValue())
                            && parentTableName.equals(tempRow.getKey().tableName())) {
                        innerMap.put(row, tempRow.getValue());
                    }
                }
            }
        }

        Set<Integer> crfDbIds = new HashSet<>();
        // 计算CRF相关信息
        for (Geometry geometry : getCRFGeom(process.getConn(), rows)) {
            crfDbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }
        for (Integer crfDbId : crfDbIds) {
            if (crfDbId.equals(sourceDbId)) {
                map.put(sourceDbId, null);
                continue;
            }

            Map<IRow, ObjStatus> result;
            if (map.containsKey(crfDbId)) {
                result = map.get(crfDbId);
            } else {
                result = new HashMap<>();
                map.put(crfDbId, result);
            }

            for (IRow row : rows) {
                if (row instanceof RdInter || row instanceof RdInterLink || row instanceof RdInterNode || row instanceof RdRoad ||
                        row instanceof RdRoadLink || row instanceof RdObject || row instanceof RdObjectInter || row instanceof RdObjectRoad
                        || row instanceof RdObjectNode || row instanceof RdObjectName || row instanceof RdObjectLink) {
                    result.put(row, defaultStatus);
                }
            }
        }

        return map;
    }

    private List<Geometry> getCRFGeom(Connection conn, List<IRow> rows) {
        Set<Integer> currInterPids = new HashSet<>();
        Set<Integer> currRoadPids = new HashSet<>();
        Set<Integer> currObjectPids = new HashSet<>();

        for (IRow row : rows) {
            if (row instanceof RdInter || row instanceof RdInterLink || row instanceof RdInterNode) {
                currInterPids.add(getCRFPid(row));
            } else if (row instanceof RdRoad || row instanceof RdRoadLink) {
                currRoadPids.add(getCRFPid(row));
            } else if (row instanceof RdObject || row instanceof RdObjectInter || row instanceof RdObjectRoad || row instanceof
                    RdObjectNode || row instanceof RdObjectName || row instanceof RdObjectLink) {
                currObjectPids.add(getCRFPid(row));
            }
        }

        List<Geometry> geoms = new ArrayList<>();

        Set<Integer> inters = new HashSet<>(currInterPids);
        Set<Integer> roads = new HashSet<>(currRoadPids);
        Set<Integer> links = new HashSet<>();
        Set<Integer> nodes = new HashSet<>();

        AbstractSelector selector = new AbstractSelector(RdObject.class, conn);
        List<IRow> rowsTmp = null;
        try {
            rowsTmp = selector.loadByIds(new ArrayList<>(currObjectPids), true, true);
            for (IRow row : rows) {
                if (row instanceof RdObject && row.status().equals(ObjStatus.INSERT)) {
                    rowsTmp.add(row);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("获取RdObject出错[ids: %s]", Arrays.toString(currObjectPids.toArray())), e);
        }

        for (IRow rowObj : rowsTmp) {
            RdObject obj = (RdObject) rowObj;
            for (IRow row : obj.getInters()) {
                inters.add(((RdObjectInter) row).getInterPid());
            }
            for (IRow row : obj.getRoads()) {
                roads.add(((RdObjectRoad) row).getRoadPid());
            }
            for (IRow row : obj.getLinks()) {
                links.add(((RdObjectLink) row).getLinkPid());
            }
            for (IRow row : obj.getNodes()) {
                nodes.add(((RdObjectNode) row).getNodePid());
            }
        }

        selector = new AbstractSelector(RdInter.class, conn);
        try {
            rowsTmp = selector.loadByIds(new ArrayList<>(inters), true, true);
            for (IRow row : rows) {
                if (row instanceof RdInter && row.status().equals(ObjStatus.INSERT)) {
                    rowsTmp.add(row);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("获取RdInter出错[ids: %s]", Arrays.toString(inters.toArray())), e);
        }

        for (IRow rowInter : rowsTmp) {
            RdInter obj = (RdInter) rowInter;
            for (IRow row : obj.getLinks()) {
                links.add(((RdInterLink) row).getLinkPid());
            }
            for (IRow row : obj.getNodes()) {
                nodes.add(((RdInterNode) row).getNodePid());
            }
        }

        selector = new AbstractSelector(RdRoad.class, conn);
        try {
            rowsTmp = selector.loadByIds(new ArrayList<>(roads), true, true);
            for (IRow row : rows) {
                if (row instanceof RdRoad && row.status().equals(ObjStatus.INSERT)) {
                    rowsTmp.add(row);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("获取RdRoad出错[ids: %s]", Arrays.toString(roads.toArray())), e);
        }

        for (IRow rowRoad : rowsTmp) {
            RdRoad obj = (RdRoad) rowRoad;
            for (IRow row : obj.getLinks()) {
                links.add(((RdRoadLink) row).getLinkPid());
            }
        }


        selector = new AbstractSelector(RdLink.class, conn);
        try {
            rowsTmp = selector.loadByIds(new ArrayList<>(links), true, false);
            if (rowsTmp.size() != links.size() && rowsTmp.size() > 0) {
                int minus = links.size() - rowsTmp.size();

                RdLink link = (RdLink) rowsTmp.get(0);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator.jts2Wkt(link.getGeometry(), Constant.BASE_SHRINK, Constant
                        .BASE_PRECISION), 3);
                for (Integer dbId : dbIds) {
                    if (dbId.equals(process.getCommand().getDbId())) {
                        continue;
                    }

                    Connection connection = null;
                    try {
                        connection = DBConnector.getInstance().getConnectionById(dbId);
                        List<RdLink> rdLinks = new RdLinkSelector(connection).loadByPids(new ArrayList<>(links), false);
                        rowsTmp.addAll(rdLinks);
                        if (rdLinks.size() == minus) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        DBUtils.closeConnection(connection);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("获取RdLink出错[ids: %s]", Arrays.toString(links.toArray())), e);
        }

        for (IRow rowLink : rowsTmp) {
            RdLink obj = (RdLink) rowLink;
            geoms.add(obj.getGeometry());
        }

        selector = new AbstractSelector(RdNode.class, conn);
        try {
            rowsTmp = selector.loadByIds(new ArrayList<>(nodes), true, false);
            if (rowsTmp.size() != nodes.size() && rowsTmp.size() > 0) {
                int minus = nodes.size() - rowsTmp.size();

                RdNode node = (RdNode) rowsTmp.get(0);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator.jts2Wkt(node.getGeometry(), Constant.BASE_SHRINK, Constant
                        .BASE_PRECISION), 3);
                for (Integer dbId : dbIds) {
                    if (dbId.equals(process.getCommand().getDbId())) {
                        continue;
                    }

                    Connection connection = null;
                    try {
                        connection = DBConnector.getInstance().getConnectionById(dbId);
                        List<IRow> rdNodes = new RdNodeSelector(connection).loadByIds(new ArrayList<>(links), false,false);
                        rowsTmp.addAll(rdNodes);
                        if (rdNodes.size() == minus) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        DBUtils.closeConnection(connection);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("获取RdNode出错[ids: %s]", Arrays.toString(nodes.toArray())), e);
        }

        for (IRow rowNode : rowsTmp) {
            RdNode obj = (RdNode) rowNode;
            geoms.add(obj.getGeometry());
        }

        return geoms;
    }

    private Integer getCRFPid(IRow row) {
        if (row instanceof RdInter) {

            return ((RdInter) row).getPid();

        } else if (row instanceof RdInterLink) {

            return ((RdInterLink) row).getPid();

        } else if (row instanceof RdInterNode) {

            return ((RdInterNode) row).getPid();

        } else if (row instanceof RdRoad) {

            return ((RdRoad) row).getPid();

        } else if (row instanceof RdRoadLink) {

            return ((RdRoadLink) row).getPid();

        } else if (row instanceof RdObject) {

            return ((RdObject) row).getPid();

        } else if (row instanceof RdObjectInter) {

            return ((RdObjectInter) row).getPid();

        } else if (row instanceof RdObjectLink) {

            return ((RdObjectLink) row).getPid();

        } else if (row instanceof RdObjectName) {

            return ((RdObjectName) row).getPid();

        } else if (row instanceof RdObjectNode) {

            return ((RdObjectNode) row).getPid();

        } else if (row instanceof RdObjectRoad) {

            return ((RdObjectRoad) row).getPid();
        } else
            return 0;
    }

    private void checkErrorOperation(Result result) throws Exception {
        JSONObject json = JSONObject.fromObject(requester);
        if (OperType.REPAIR.equals(operType)) {
            for (Map.Entry<ObjType, Class<? extends IRow>> entry : Constant.OBJ_TYPE_CLASS_MAP.entrySet()) {
                if (!entry.getKey().equals(objType)) {
                    continue;
                }
                if (!json.containsKey("data") || !json.getJSONObject("data").containsKey("catchInfos")) {
                    return;
                }

                Iterator<JSONObject> iterator = json.getJSONObject("data").getJSONArray("catchInfos").iterator();
                while (iterator.hasNext()) {
                    JSONObject obj = iterator.next();
                    if (obj.containsKey("nodePid")) {
                        IRow link = new AbstractSelector(entry.getValue(), process.getConn()).loadById(obj.getInt("nodePid"), false);
                        Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeometryUtils.loadGeometry(link));
                        if (dbIds.size() > 1) {
                            throw new Exception("不允分离大区库接边Node!");
                        }
                    }
                }
            }
        }
        if (OperType.DELETE.equals(operType) && (Constant.LINK_TYPES.containsKey(objType) || Constant.NODE_TYPES.containsKey(objType))) {
            if (json.containsKey("objId")) {
                int objId = json.getInt("objId");

                for (IRow row : result.getDelObjects()) {
                    for (Map.Entry<ObjType, Class<? extends IRow>> entry : Constant.NODE_TYPES.entrySet()) {
                        if (!row.objType().equals(entry.getKey()) || (row.parentPKValue() == objId)) {
                            continue;
                        }

                        IRow node = new AbstractSelector(entry.getValue(), process.getConn()).loadById(row.parentPKValue(), false);
                        Geometry geometry = GeometryUtils.loadGeometry(node);
                        Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(geometry);
                        if (dbIds.size() > 1) {
                            throw new Exception("该操作会导致跨大区Node删除，请优先执行跨大区Node删除操作!");
                        }
                    }
                }
            }
        }
    }

    /**
     * 执行操作
     *
     * @return
     * @throws Exception
     */
    public String run() throws Exception {
        List<AbstractProcess> processes = new ArrayList<>();
        AbstractCommand abstractCommand = TransactionFactory.generateCommand(this, requester);
        AbstractProcess abstractProcess = createProcess(abstractCommand);
        processes.add(abstractProcess);
        if (abstractCommand.isHasConn() && null != conn) {
            conn.setAutoCommit(false);
            abstractProcess.setConn(conn);
        }

        String msg = "";
        try {
            process = processes.iterator().next();

            msg = process.run();

            Result result = process.getResult();

            // 处理提示信息
            if (infect == 1) {
                if (StringUtils.isNotEmpty(msg)) {
                    return msg;
                }
                return delPrompt(result);
            }

            // 检查接边点不允许分离
            checkErrorOperation(result);

            //EdgeResult edge = new EdgeResult(requester);
            // 初始化新增数据RowId，保证接边库数据一致
            EdgeUtil.initalizeRowId(result.getAddObjects());
            //edge.setSourceResult(result);

            Integer sourceDbId = Integer.valueOf(process.getCommand().getDbId());
            //edge.setSourceDb(sourceDbId);
            //EdgeOperation edgeOperation = new EdgeOperation();
            //edgeOperation.getAbstractProcesses().add(process);
            //edgeOperation.setTransaction(this);

            //edgeOperation.handleEdge(edge);

            boolean hasSourceDb = false;

            // 跨大区处理6种点要素以及所对应线要素
            if (Constant.FACE_TYPES.containsKey(objType) || Constant.LINK_TYPES.containsKey(objType)
                    || Constant.NODE_TYPES.containsKey(objType) || Constant.CRF_TYPES.contains(objType)) {

                // 检查操作结果是否产生接边影响
                Map<Integer, Result> map = calcDbIdsRefResult(result, sourceDbId);
                if (map.containsKey(sourceDbId)) {
                    hasSourceDb = true;
                    map.remove(sourceDbId);
                }

                if (!map.isEmpty()) {
                    switch (operType) {
                        case DELETE: execEdgeDelete(processes, map); break;
                        case MOVE: execEdgeMove(processes, map); break;
                        default: execEdgeDefault(processes, map);
                    }
                }
            }

            if (!Constant.CRF_TYPES.contains(objType) || hasSourceDb) {
                // WEB传入目标库写入数据、履历
                recordData(process, result);
            }

            // 执行后检查
            process.postCheck();

            // 数据入库
            for (AbstractProcess process : processes) {
                process.getConn().commit();
            }
        } catch (Exception e) {
            logger.error(String.format("%s操作失败，数据库进行回滚，requester: %s", objType, requester), e);
            for (AbstractProcess process : processes) {
                DBUtils.rollBack(process.getConn());
            }
            throw e;
        } finally {
            for (AbstractProcess process : processes) {
                DBUtils.closeConnection(process.getConn());
            }
        }
        return msg;
    }

    private void execEdgeMove(List<AbstractProcess> processes, Map<Integer, Result> map) throws Exception {
        JSONObject json = JSONObject.fromObject(requester);

        for (Map.Entry<Integer, Result> entry : map.entrySet()) {
            Result res = entry.getValue();
            List<IRow> updateObjectes = res.getUpdateObjects();
            if (updateObjectes.isEmpty()) {
                execEdgeDefault(processes, map);
                continue;
            }
            for (IRow updateObject : updateObjectes) {
                if (Constant.NODE_TYPES.containsKey(updateObject.objType())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("command", operType.toString());
                    jsonObject.put("dbId", entry.getKey());
                    jsonObject.put("subtaskId", json.getInt("subtaskId"));
                    jsonObject.put("type", updateObject.objType());
                    jsonObject.put("objId", ((IObj) updateObject).pid());

                    JSONObject data = new JSONObject();
                    if (!updateObject.changedFields().containsKey("geometry")) {
                        continue;
                    }
                    Geometry geometry = GeoTranslator.geojson2Jts((JSONObject) updateObject.changedFields().get("geometry"));
                    data.put("longitude", geometry.getCoordinate().x);
                    data.put("latitude", geometry.getCoordinate().y);
                    jsonObject.put("data", data);

                    AbstractCommand command = TransactionFactory.generateCommand(this, jsonObject.toString());
                    AbstractProcess process = createProcess(command);
                    processes.add(process);
                    process.run();

                    this.recordData(process, process.getResult());
                }
            }
        }
    }

    private void execEdgeDelete(List<AbstractProcess> processes, Map<Integer, Result> map) throws Exception {
        JSONObject json = JSONObject.fromObject(requester);

        Result otherResult = null;

        for (Map.Entry<Integer, Result> entry : map.entrySet()) {
            AbstractProcess process = null;

            Result res = entry.getValue();
            List<IRow> delObjects = res.getDelObjects();
            for (IRow deleteRow : delObjects) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dbId", entry.getKey());
                jsonObject.put("command", operType.toString());
                jsonObject.put("subtaskId", json.getInt("subtaskId"));

                if (Constant.NODE_TYPES.containsKey(deleteRow.objType()) || Constant.LINK_TYPES.containsKey(deleteRow.objType())) {
                    jsonObject.put("type", deleteRow.objType());
                    jsonObject.put("objId", ((IObj) deleteRow).pid());

                    AbstractCommand command = TransactionFactory.generateCommand(this, jsonObject.toString());
                    process = createProcess(command);
                    processes.add(process);
                    process.run();

                    otherResult = process.getResult();
                    this.recordData(process, process.getResult());
                }
            }

            Result crfResult = new Result();
            for (IRow iRow : res.getAddObjects()) {
                if (Constant.CRF_TYPES.contains(iRow.objType())) {
                    if (null != otherResult && EdgeUtil.contains(otherResult.getAddObjects(), iRow)) {
                        continue;
                    }
                    crfResult.getAddObjects().add(iRow);
                    crfResult.getListAddIRowObPid().add(iRow.parentPKValue());
                }
            }
            for (IRow iRow : res.getUpdateObjects()) {
                if (Constant.CRF_TYPES.contains(iRow.objType())) {
                    if (null != otherResult && EdgeUtil.contains(otherResult.getUpdateObjects(), iRow)) {
                        continue;
                    }
                    crfResult.getUpdateObjects().add(iRow);
                    crfResult.getListUpdateIRowObPid().add(iRow.parentPKValue());
                }
            }
            for (IRow iRow : res.getDelObjects()) {
                if (Constant.CRF_TYPES.contains(iRow.objType())) {
                    if (null != otherResult && EdgeUtil.contains(otherResult.getDelObjects(), iRow)) {
                        continue;
                    }
                    crfResult.getDelObjects().add(iRow);
                    crfResult.getListDelIRowObPid().add(iRow.parentPKValue());
                }
            }
            if (null == process) {
                AbstractCommand command = TransactionFactory.generateCommand(this, json.element("dbId", entry.getKey()).toString());
                process = createProcess(command);
                processes.add(process);
            }
            this.recordData(process, crfResult);

            AbstractProcess mainProcess = processes.get(0);
            if (null != otherResult) {
                for (IRow iRow : otherResult.getAddObjects()) {
                    if (Constant.CRF_TYPES.contains(iRow.objType()) && EdgeUtil.notContains(mainProcess.getResult().getAddObjects(), iRow)) {
                        mainProcess.getResult().getAddObjects().add(iRow);
                        mainProcess.getResult().getListAddIRowObPid().add(iRow.parentPKValue());
                    }
                }
                for (IRow iRow : otherResult.getUpdateObjects()) {
                    if (Constant.CRF_TYPES.contains(iRow.objType()) && EdgeUtil.notContains(mainProcess.getResult().getUpdateObjects(), iRow)) {
                        mainProcess.getResult().getUpdateObjects().add(iRow);
                        mainProcess.getResult().getListUpdateIRowObPid().add(iRow.parentPKValue());
                    }
                }
                for (IRow iRow : otherResult.getDelObjects()) {
                    if (Constant.CRF_TYPES.contains(iRow.objType()) && EdgeUtil.notContains(mainProcess.getResult().getDelObjects(), iRow)) {
                        mainProcess.getResult().getDelObjects().add(iRow);
                        mainProcess.getResult().getListDelIRowObPid().add(iRow.parentPKValue());
                    }
                }
            }
        }
    }

    private void execEdgeDefault(List<AbstractProcess> processes, Map<Integer, Result> map) throws Exception {
        JSONObject json = JSONObject.fromObject(requester);

        for (Map.Entry<Integer, Result> entry : map.entrySet()) {
            AbstractCommand command = TransactionFactory.generateCommand(this, json.element("dbId", entry.getKey()).toString());

            AbstractProcess process = createProcess(command);
            processes.add(process);

            Result res = entry.getValue();
            this.recordData(process, res);
        }
    }

    /**
     * 执行操作
     *
     * @return
     * @throws Exception
     */
    public String innerRun() throws Exception {
        List<AbstractProcess> processes = new ArrayList<>();
        AbstractCommand abstractCommand = TransactionFactory.generateCommand(this, requester);
        AbstractProcess abstractProcess = createProcess(abstractCommand);
        processes.add(abstractProcess);
        if (abstractCommand.isHasConn() && null != conn) {
            conn.setAutoCommit(false);
            abstractProcess.setConn(conn);
        }

        String msg = "";
        try {
            process = processes.iterator().next();

            msg = process.innerRun();

            Result result = process.getResult();

            // 处理提示信息
            if (infect == 1) {
                if (StringUtils.isNotEmpty(msg)) {
                    return msg;
                }
                return delPrompt(result);
            }

            // 初始化新增数据RowId，保证接边库数据一致
            EdgeUtil.initalizeRowId(result.getAddObjects());

            // 跨大区处理6种点要素以及所对应线要素
            if (Constant.LINK_TYPES.containsKey(objType) || Constant.NODE_TYPES.containsKey(objType) || Constant.CRF_TYPES.contains(objType)) {
                Integer sourceDbId = Integer.valueOf(process.getCommand().getDbId());

                // 检查操作结果是否产生接边影响
                Map<Integer, Result> map = calcDbIdsRefResult(result, sourceDbId);

                if (!map.isEmpty()) {
                    switch (operType) {
                        //case CREATE: execEdgeDefault(processes, map); break;
                        case DELETE: execEdgeDelete(processes, map); break;
                        case MOVE: execEdgeMove(processes, map); break;
                        default: execEdgeDefault(processes, map);
                    }
                }
            }

            // 写入数据、履历
            recordData(process, result);

            process.postCheck();

            // 数据入库
            for (AbstractProcess process : processes) {
                process.getConn().commit();
            }
        } catch (Exception e) {
            logger.error(String.format("%s操作失败，数据库进行回滚，requester: %s", objType, requester), e);
            for (AbstractProcess process : processes) {
                DBUtils.rollBack(process.getConn());
            }
            throw e;
        } finally {
            for (AbstractProcess process : processes) {
                if (process.getConn() == conn) {
                    continue;
                }
                DBUtils.closeConnection(process.getConn());
            }
        }
        return msg;
    }

    /**
     * 生成履历，写入数据
     *
     * @param process
     * @param result
     * @return
     * @throws Exception
     */
    public boolean recordData(AbstractProcess process, Result result) throws Exception {
        AbstractCommand command = process.getCommand();
        LogWriter lw = new LogWriter(process.getConn());
        lw.setUserId(command.getUserId());
        lw.setTaskId(command.getTaskId());
        lw.generateLog(command, result);
        OperatorFactory.recordData(process.getConn(), result);
        lw.recordLog(command, result);
        try {
            PoiMsgPublisher.publish(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    private String delPrompt(Result result) {
        Map<String, List<AlertObject>> infects = new HashMap();
        List<IRow> addList = result.getAddObjects();
        List<IRow> delList = result.getDelObjects();
        List<IRow> updateList = result.getUpdateObjects();
        List<IObj> addObj = new ArrayList<>();
        List<IObj> delObj = new ArrayList<>();
        List<IObj> updateObj = new ArrayList<>();
        // 添加对新增要素的影响
        this.convertObj(addList, addObj);
        this.convertObj(updateList, updateObj);
        this.convertObj(delList, delObj);
        infects.putAll(this.sort(addObj, ObjStatus.INSERT));
        infects.putAll(this.sort(updateObj, ObjStatus.UPDATE));
        infects.putAll(this.sort(delObj, ObjStatus.DELETE));
        logger.info("删除影响：" + JSONObject.fromObject(infects).toString());
        return JSONObject.fromObject(infects).toString();
    }

    private void convertObj(List<IRow> rows, List<IObj> objs) {
        for (IRow row : rows) {
            if (row instanceof IObj) {
                IObj obj = (IObj) row;
                objs.add(obj);
            }
        }
    }

    /**
     * 操作結果排序
     *
     * @param objs
     * @param status
     * @return
     */
    private Map<String, List<AlertObject>> sort(List<IObj> objs, ObjStatus status) {
        Map<String, List<AlertObject>> tm = new HashMap();
        for (IObj obj : objs) {
            if (tm.containsKey(ObjStatus.getCHIName(status).concat(obj.objType().toString()))) {
                AlertObject object = new AlertObject(obj.objType(), obj.pid(), status);
                List<AlertObject> list = tm.get(ObjStatus.getCHIName(status).concat(obj.objType().toString()));
                list.add(object);
            } else {
                AlertObject object = new AlertObject(obj.objType(), obj.pid(), status);
                List<AlertObject> tem = new ArrayList<>();
                tem.add(object);
                tm.put(ObjStatus.getCHIName(status).concat(obj.objType().toString()), tem);
            }
        }
        return tm;
    }

    /**
     * Getter method for property <tt>logs</tt>.
     *
     * @return property value of logs
     */
    public String getLogs() {
        return process.getResult().getLogs();
    }

    /**
     * Getter method for property <tt>checkLog</tt>.
     *
     * @return property value of checkLog
     */
    public JSONArray getCheckLog() {
        return process.getResult().getCheckResults();

    }

    /**
     * Getter method for property <tt>pid</tt>.
     *
     * @return property value of pid
     */
    public int getPid() {
        return process.getResult().getPrimaryPid();
    }

    /**
     * Getter method for property <tt>dbType</tt>.
     *
     * @return property value of dbType
     */
    public int getDbType() {
        return dbType;
    }
}
