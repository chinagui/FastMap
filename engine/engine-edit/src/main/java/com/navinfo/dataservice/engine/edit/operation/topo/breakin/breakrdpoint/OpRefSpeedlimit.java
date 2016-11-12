package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class OpRefSpeedlimit implements IOperation {

	private Command command;

	private Result result;

	public OpRefSpeedlimit(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleSpeedlimit(command.getSpeedlimits());

		return null;
	}

	// 处理点限速
	private void handleSpeedlimit(List<RdSpeedlimit> list) throws Exception {

		for (RdSpeedlimit limit : list) {

			int inLinkPid = 0;

			double distanceFlag = Double.MAX_VALUE;

			for (RdLink link : command.getNewLinks()) {

				double distance = limit.getGeometry().distance(
						link.getGeometry());

				if (distance < distanceFlag) {

					distanceFlag = distance;

					inLinkPid = link.getPid();
				}
			}

			limit.changedFields().put("linkPid", inLinkPid);

			result.insertObject(limit, ObjStatus.UPDATE, limit.pid());
		}
	}

}
