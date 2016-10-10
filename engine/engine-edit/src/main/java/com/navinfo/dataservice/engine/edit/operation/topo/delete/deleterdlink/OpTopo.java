package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

public class OpTopo implements IOperation {

	private Command command;

	public OpTopo(Command command) {

		this.command = command;
	}

	@Override
	public String run(Result result) {

		String msg = null;

		RdLink link = command.getLink();

		result.setPrimaryPid(link.getPid());

		result.insertObject(link, ObjStatus.DELETE, link.pid());

		for (RdNode node : command.getNodes()) {

			result.insertObject(node, ObjStatus.DELETE, node.pid());
		}

		return msg;
	}

	public List<AlertObject> getDeleteLinkInfectData(RdLink link, Connection conn) throws Exception {

		AlertObject alertObj = new AlertObject();

		alertObj.setObjType(link.objType());

		alertObj.setPid(link.getPid());

		alertObj.setStatus(ObjStatus.DELETE);

		List<AlertObject> alertList = new ArrayList<>();

		if(!alertList.contains(alertObj))
		{
			alertList.add(alertObj);
		}

		return alertList;
	}

	public List<AlertObject> getDeleteNodeInfectData(int linkPid, Connection conn) throws Exception {
		RdNodeSelector selector = new RdNodeSelector(conn);

		List<RdNode> nodeList = selector.loadEndRdNodeByLinkPid(linkPid, false);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdNode node : nodeList) {
			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(node.objType());

			alertObj.setPid(node.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
