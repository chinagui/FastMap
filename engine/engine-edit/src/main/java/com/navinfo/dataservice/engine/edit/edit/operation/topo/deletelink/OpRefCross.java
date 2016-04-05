package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

public class OpRefCross implements IOperation {

	private Command command;

	public OpRefCross(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		List<Integer> nodePids = command.getNodePids();

		for (RdCross cross : command.getCrosses()) {

			List<RdCrossNode> nodes = new ArrayList<RdCrossNode>();

			for (IRow row : cross.getNodes()) {
				RdCrossNode node = (RdCrossNode) row;

				if (nodePids.contains(node.getNodePid())) {
					nodes.add(node);
				}
			}

			if (nodes.size() == cross.getNodes().size()) {
				result.insertObject(cross, ObjStatus.DELETE, cross.pid());
			} else {
				for (RdCrossNode node : nodes) {
					result.insertObject(node, ObjStatus.DELETE, cross.pid());
				}

				for (IRow row : cross.getLinks()) {
					RdCrossLink link = (RdCrossLink) row;

					if (link.getLinkPid() == command.getLinkPid()) {
						result.insertObject(link, ObjStatus.DELETE, cross.pid());
						break;
					}
				}
			}

		}

		return null;
	}

}
