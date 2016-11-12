package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
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

//			if (limit.getGeometry().distance(command.getLink1().getGeometry()) < limit
//					.getGeometry().distance(command.getLink2().getGeometry())) {
//
//				inLinkPid = command.getLink1().getPid();
//			} else {
//				
//				inLinkPid = command.getLink2().getPid();
//			}

			limit.changedFields().put("linkPid", inLinkPid);

			result.insertObject(limit, ObjStatus.UPDATE, limit.pid());
		}
	}

}
