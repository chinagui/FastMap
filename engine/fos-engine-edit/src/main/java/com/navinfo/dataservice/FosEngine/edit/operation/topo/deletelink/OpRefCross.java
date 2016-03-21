package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
				result.insertObject(cross, ObjStatus.DELETE);
			} else {
				for (RdCrossNode node : nodes) {
					result.insertObject(node, ObjStatus.DELETE);
				}

				for (IRow row : cross.getLinks()) {
					RdCrossLink link = (RdCrossLink) row;

					if (link.getLinkPid() == command.getLinkPid()) {
						result.insertObject(link, ObjStatus.DELETE);
						break;
					}
				}
			}

		}

		return null;
	}

}
