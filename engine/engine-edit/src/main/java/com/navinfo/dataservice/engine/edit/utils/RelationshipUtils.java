package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * 删除rdlink、rdnode维护关联要素
 * Created by ly on 2017/5/5.
 */
public class RelationshipUtils {

    private Connection conn = null;

    private Result result = null;

    private List<Integer> delLinkPids = new ArrayList<>();

    private List<Integer> delNodePids = new ArrayList<>();

    private List<RdLink> delLinks = new ArrayList<>();

    public RelationshipUtils(Connection conn, Result result) {

        this.conn = conn;

        this.result = result;
    }

    private void init(List<Integer> delLinkPids, List<Integer> delNodePids, List<RdLink> delLinks) {

        this.delLinkPids = delLinkPids;

        this.delNodePids = delNodePids;

        this.delLinks = delLinks;
    }

    /**
     * 维护关联要素
     * @param delLinkPids  被删linkpid列表
     * @param delNodePids 被删nodepid列表
     * @param delLinks 被删link 对象列表
     * @throws Exception
     */
    public void handleRelationObj(List<Integer> delLinkPids, List<Integer> delNodePids, List<RdLink> delLinks) throws Exception {

        if (this.conn == null || this.result == null) {

            return;
        }

        init(delLinkPids, delNodePids, delLinks);

        handleTMC();

        handleRdCross();

        handleRdTrafficsignal();

        handleRdRestriction();

        handleRdLaneConnexity();

        handleRdVoiceguide();

        handleRdDirectroute();

        handleRdBranch();

        handleRdSE();

        handleRdGate();

        handleRdTollgate();

        handleRdSlope();

        handleRdVariableSpeed();

        handleRdSpeedbump();

        handleRdWarninginfo();

        handleRdLinkWarning();

        handleRdElectroniceye();

        handleRdSpeedlimit();

        handleRdMileagepile();

        handleRdHgwgLimit();

        handleRdGsc();

        handleRdLane();

        handleCRF();

        handleRDSameNode();

        handleRDSameLink();

        handleIxPoi();

        handleAdAdmin();
    }

    /**
     * TMC
     *
     * @throws Exception
     */
    private void handleTMC() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation(
                this.conn);

        operation.deleteLinkUpdateTmc(result, delLinks, null);
    }

    /**
     * 路口
     */
    private void handleRdCross() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Operation(
                this.conn);

        operation.updateCrossByNodeLink(result, delNodePids, delLinkPids);
    }

    /**
     * 信号灯
     */
    private void handleRdTrafficsignal() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
                conn);

        operation.deleteByLinks(result, delLinkPids);
    }

    /**
     * 交限
     */
    private void handleRdRestriction() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation(this.conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 车信
     */
    private void handleRdLaneConnexity() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation(
                this.conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 语音引导
     */
    private void handleRdVoiceguide() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(
                conn);

        operation.deleteByLinks(delLinkPids,result);
    }


    /**
     * 顺行
     */
    private void handleRdDirectroute() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
                conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 分歧
     */
    private void handleRdBranch() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation(conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 分岔口提示
     */
    private void handleRdSE() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }

    /**
     * 大门
     */
    private void handleRdGate() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation(
                conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 收费站
     */
    private void handleRdTollgate() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }

    /**
     * 坡度
     */
    private void handleRdSlope() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }

    /**
     * 可变限速
     *
     * @throws Exception
     */
    private void handleRdVariableSpeed() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete.Operation(
                this.conn);

        operation.deleteByLinks(delLinkPids, result);
    }


    /**
     * 减速带
     *
     * @throws Exception
     */
    private void handleRdSpeedbump() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }
    /**
     * 警示信息 RD_LINK_WARNING
     *
     * @throws Exception
     */
    private void handleRdLinkWarning() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation(
                conn);

        operation.updateByLinks(delLinkPids, result);
    }

    /**
     * 警示信息
     *
     * @throws Exception
     */
    private void handleRdWarninginfo() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
                conn);

        operation.deleteByLinks(delLinkPids, result);
    }

    /**
     * 电子眼
     *
     * @throws Exception
     */
    private void handleRdElectroniceye() throws Exception {

        // 删除所有与linkPids关联的电子眼
        com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }


    /**
     * 限速关系
     *
     * @throws Exception
     */
    private void handleRdSpeedlimit() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation(this.conn);

        operation.deleteByLinks(delLinkPids, result);
    }


    /**
     * 里程桩
     *
     * @throws Exception
     */
    private void handleRdMileagepile() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Operation(this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }


    /**
     * 限高限重
     *
     * @throws Exception
     */
    private void handleRdHgwgLimit() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Operation(
                this.conn);

        operation.deleteByLinks(result, delLinkPids);
    }

    /**
     * 立交
     *
     * @throws Exception
     */
    private void handleRdGsc() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation gscDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
                this.conn);

        gscDelOption.deleteByLinkPid(delLinks, result);
    }

    /**
     * 详细车道、详细车道连通
     *
     * @throws Exception
     */
    private void handleRdLane() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
                conn);

        operation.deleteRdLaneforRdLinks(delLinkPids, result);

    }

    /**
     * CRF交叉点、CRF道路、CRF道路对象 (RdInter、RdRoad、RdObject)
     *
     * @throws Exception
     */
    private void handleCRF() throws Exception {

        com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils rdCRFOperateUtils = new com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils(this.conn);

        rdCRFOperateUtils.delNodeLink(result, delLinkPids, delNodePids);
    }

    /**
     * 同一点
     *
     * @throws Exception
     */
    private void handleRDSameNode() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
                this.conn);

        rdinterOperation.deleteByNodes(delNodePids, "RD_NODE", result);
    }

    /**
     * 同一线
     *
     * @throws Exception
     */
    private void handleRDSameLink() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
                conn);

        if (delLinks.size() < 1) {
            return;
        }

        operation.deleteByLinks(delLinkPids, delLinks.get(0).tableName(), result);
    }

    /**
     * Poi
     *
     * @throws Exception
     */
    private void handleIxPoi() throws Exception {

        // poi被动维护（引导link）
        com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation deletePoiOperation = new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Operation(
                this.conn);

        deletePoiOperation.deleteByLinks(delLinkPids, result);

    }

    /**
     * 行政区划代表点
     *
     * @throws Exception
     */
    private void handleAdAdmin() throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Operation(this.conn);

        operation.deleteByLinks(delLinkPids, result);
    }
}
