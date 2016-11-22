package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRelationObj {

    private Connection conn;

    public OpRefRelationObj(Connection conn) {
        this.conn = conn;
    }

    /**
     * 处理车信
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdLaneconnexity(Command command, List<RdLink> newLinks,
                                        Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理交限
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdRestriction(Command command, List<RdLink> newLinks,
                                      Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理分歧
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdBranch(Command command, List<RdLink> newLinks,
                                 Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理大门
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdGate(Command command, List<RdLink> newLinks,
                               Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理收费站
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdTollgate(Command command, List<RdLink> newLinks,
                                   Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理分叉提示
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdSe(Command command, List<RdLink> newLinks,
                             Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理顺行
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdDirectroute(Command command, List<RdLink> newLinks,
                                      Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理语音引导
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdVoiceguide(Command command, List<RdLink> newLinks,
                                     Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理警示信息
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdWarninginfo(Command command, List<RdLink> newLinks,
                                      Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理减速带
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdSpeedbump(Command command, List<RdLink> newLinks,
                                    Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理坡度
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdSlope(Command command, List<RdLink> newLinks,
                                Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理可变限速
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdVariableSpeed(Command command, List<RdLink> newLinks,
                                        Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update.Operation(
                this.conn);

        operation.departNode(command.getRdLink(), command.getNodePid(),
                newLinks, result);

        return null;
    }

    /**
     * 处理限高限重
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdHgwgLimit(Command command, List<RdLink> newLinks, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.depart.Operation(this.conn);
        operation.depart(command.getRdLink(), newLinks, result);
        return null;
    }

    /**
     * 处理里程桩
     *
     * @param command
     * @param newLinks
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdMileagepile(Command command, List<RdLink> newLinks, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.depart.Operation(this.conn);
        operation.depart(command.getRdLink(), newLinks, result);
        return null;
    }
}
