package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;

public class OpTopo implements IOperation {

	private Command command;

	public OpTopo(Command command) {

		this.command = command;
	}

	@Override
	public String run(Result result) {

		String msg = null;

		for (RdNode node : command.getNodes()) {

			result.insertObject(node, ObjStatus.DELETE, node.pid());

			result.setPrimaryPid(node.getPid());
		}

		for (RdLink link : command.getLinks()) {

			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}

		return msg;
	}

	public List<AlertObject> getDeleteNodeInfectData(int nodePid, Connection conn) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		AlertObject alertObj = new AlertObject();

		alertObj.setObjType(ObjType.RDNODE);

		alertObj.setPid(nodePid);

		alertObj.setStatus(ObjStatus.DELETE);

		alertList.add(alertObj);

		return alertList;
	}
}
