package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;

public class OpRefRelationObj {

    private Connection conn = null;

    public OpRefRelationObj(Connection conn) {
        this.conn = conn;
    }

    /**
     * 同一线
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleSameLink(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(conn);
        operation.deleteByUpDownPartLink(command.getsNode().getPid(), command.geteNode().getPid(), command.getLinks()
                , result);
        return "";
    }

    /**
     * 同一点
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleSameNode(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(conn);
        operation.deleteByUpDownPartLink(command.getsNode().getPid(), command.geteNode().getPid(), command.getLinks()
                , result);
        return "";
    }

    /**
     * 道路点
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdNode(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdnode.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdnode.depart.Operation(conn);
        operation.updownDepart(command.getsNode().getPid(), command.geteNode().getPid(), command.getLinks(), result);
        return "";
    }
 
    /**
     * 车信
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleLaneConnexity(Command command, Result result) throws Exception {

        int preNodePid = command.getsNode().getPid();

        int lastNodePid = command.geteNode().getPid();

        com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.depart.Operation operation = new com
                .navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.depart.Operation(conn, preNodePid,
                lastNodePid, command.getNoTargetLinks(), command.getLinks());

        operation.upDownPart(command.getLeftLinkMapping(), command.getRightLinkMapping(), result);
        return "";
    }

    /**
     * 交限
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRestriction(Command command, Result result) throws Exception {

        int preNodePid = command.getsNode().getPid();

        int lastNodePid = command.geteNode().getPid();

        com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdrestriction.depart.Operation(conn, preNodePid, lastNodePid,
                command.getNoTargetLinks(), command.getLinks());

        operation.upDownPart(command.getLeftLinkMapping(), command.getRightLinkMapping(), result);

        return "";
    }

    /**
     * 语音引导
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleVoiceguide(Command command, Result result) throws Exception {

        int preNodePid = command.getsNode().getPid();

        int lastNodePid = command.geteNode().getPid();

        com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdvoiceguide.depart.Operation(conn, preNodePid, lastNodePid,
                command.getNoTargetLinks(), command.getLinks());

        operation.upDownPart(command.getLeftLinkMapping(), command.getRightLinkMapping(), result);

        return "";
    }
 
    /**
     * 分歧
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleBranch(Command command, Result result) throws Exception {

        int preNodePid = command.getsNode().getPid();

        int lastNodePid = command.geteNode().getPid();

        com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdbranch.depart.Operation(conn, preNodePid, lastNodePid,
                command.getNoTargetLinks(), command.getLinks());

        operation.upDownPart(command.getLeftLinkMapping(), command.getRightLinkMapping(), result);

        return "";
    }

    /**
     * 顺行
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleDirectroute(Command command, Result result) throws Exception {

        int preNodePid = command.getsNode().getPid();

        int lastNodePid = command.geteNode().getPid();

        com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rddirectroute.depart.Operation(conn, preNodePid, lastNodePid,
                command.getNoTargetLinks(), command.getLinks());

        operation.upDownPart(command.getLeftLinkMapping(), command.getRightLinkMapping(), result);

        return "";
    }

    /**
     * 警示信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerdWarninginfo(Command command, Result result) throws Exception {
       
        com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdwarninginfo.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , command.getNoTargetLinks(), result);
    }

    /**
     * 警示信息RD_LINK_WARNING
     */
    public String handlerdRdLinkWarning(Command command, Result result) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdlinkwarning.update.Operation(conn);

        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 点限速
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerdSpeedlimit(Command command, Result result) throws Exception {
        
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdspeedlimit.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 维护电子眼信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerdRdElectroniceye(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdeleceye.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 维护RTIC信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdLinkRtic(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdlink.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdlink.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , result);
    }

    /**
     * 维护TMC信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdLinkTmc(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.tmc.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.tmc.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , command.getNoTargetLinks(), result);
    }

    /**
     * 维护IxPoi信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerIxPoi(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.poi.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.poi.depart.Operation(conn);


        if (command.getNodeInnerLinkMap().size() > 0) {
            CoordinateList cList = new CoordinateList();

            for (int linkPid : command.getLinkPids()) {
                Geometry linkGeo = GeoTranslator.transform(command.getRightLinkMapping().get(linkPid).getGeometry(),
                        0.00001, 5);

                Coordinate[] coordinates = linkGeo.getCoordinates();

                cList.add(coordinates, false);
            }

            for (int i = command.getLinkPids().size() - 1; i >= 0; i--) {
                int linkPid = command.getLinkPids().get(i);

                Geometry linkGeo = GeoTranslator.transform(command.getLeftLinkMapping().get(linkPid).getGeometry(),
                        0.00001, 5);

                Coordinate[] coordinates = linkGeo.getCoordinates();

                cList.add(coordinates, false);
            }

            Geometry spatial = GeoTranslator.getPolygonToPoints(cList.toCoordinateArray());

            operation.updownDepartInnerPoi(spatial, command.getNodeInnerLinkMap(), command.getNoTargetLinks(), result);
        }

        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , result);
    }

    /**
     * 维护减速带信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdSpeedbump(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdspeedbump.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , result);
    }

    /**
     * 维护坡度信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdSlope(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdslope.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdslope.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , result);
    }

    /**
     * 维护可变限速
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
	public String handlerRdVariableSpeed(Command command, Result result)
			throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart.Operation(
				conn);
		return operation.updownDepart(command.getLinks(), result);
	}
	
	/**
	 * 维护信号灯
	 * @param command
	 * @param result
	 * @return
	 * @throws Exception
	 */
    public String handlerRdTrafficsignal(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.trafficsignal.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , command.getsNode(), command.geteNode(), result);
    }
 
    /**
     * 维护路口
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdCross(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdcross.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdcross.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , result);
    }

    /**
     * 维护行政区划代表点
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerAdadmin(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.adadmin.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.adadmin.depart.Operation(conn);
        return operation.updownDepart(command.getLinks(), command.getLeftLinkMapping(), command.getRightLinkMapping()
                , command.getNoTargetLinks(), command.getNodeInnerLinkMap(), result);
    }

    /**
     * 维护大门信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdGate(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdgate.delete.Operation(conn);
        return operation.updownDepart(command.getLinkPids(), result);

    }

    /**
     * 维护详细车道信息和联通
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdLane(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdlane.delete.Operation(conn);
        operation.deleteRdLaneforRdLinks(command.getLinkPids(), result);
        return "";
    }

    /**
     * 维护限高限重信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdHgwgLimit(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.hgwg.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 维护里程桩信息
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdMileagepile(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.mileagepile.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 维护收费站
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdTollgate(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdtollgate.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

    /**
     * 维护立交
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handleRdGsc(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdgsc.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

   
    /**
     *  维护分叉口提示
     * @param command
     * @param result
     * @return
     * @throws Exception
     */
    public String handlerRdSe(Command command, Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.rdse.depart.Operation operation = new com.navinfo
                .dataservice.engine.edit.operation.obj.rdse.depart.Operation(conn);
        return operation.updownDepart(command.getsNode(), command.getLinks(), command.getLeftLinkMapping(), command
                .getRightLinkMapping(), command.getNoTargetLinks(), result);
    }

	/**
	 * 维护RdRoad、RdInter、RdObject
	 * 
	 * @return
	 */
	public String handCRF(Command command, Result result) throws Exception {

		com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils operation = new com.navinfo.dataservice.engine.edit.utils.RdCRFOperateUtils(
				conn);
		
		return operation.updownDepart(result, command.getLinks(), command
				.getsNode().getPid(), command.geteNode().getPid());
	}
}
