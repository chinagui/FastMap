package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		msg = createDirectroute(result);

		return msg;
	}

	private String createDirectroute(Result result) throws Exception {

		RdDirectroute directroute = new RdDirectroute();

		CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils(conn);

		directroute.setPid(PidUtil.getInstance().applyRdDirectroutePid());

		directroute.setInLinkPid(command.getInLinkPid());

		directroute.setNodePid(command.getNodePid());

		directroute.setOutLinkPid(command.getOutLinkPid());

		directroute.setRelationshipType(calLinkOperateUtils
				.getRelationShipType(command.getNodePid(),
						command.getOutLinkPid()));

		List<Integer> viaLinks = calLinkOperateUtils.calViaLinks(conn,
				command.getInLinkPid(), command.getNodePid(),
				command.getOutLinkPid());

		int seqNum = 1;

		List<IRow> vias = new ArrayList<IRow>();

		for (Integer linkPid : viaLinks) {
			RdDirectrouteVia via = new RdDirectrouteVia();

			via.setPid(directroute.getPid());

			via.setLinkPid(linkPid);

			via.setSeqNum(seqNum);

			vias.add(via);

			seqNum++;
		}

		directroute.setVias(vias);

		result.insertObject(directroute, ObjStatus.INSERT, directroute.pid());

		return null;
	}
}
