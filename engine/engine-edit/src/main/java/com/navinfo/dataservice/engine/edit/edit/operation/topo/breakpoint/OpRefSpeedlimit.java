package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

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

		for (RdSpeedlimit rr : list) {
			Map<String, Object> changedFields = rr.changedFields();

			int inLinkPid = 0;

			if (rr.getGeometry().distance(command.getLink1().getGeometry()) < rr
					.getGeometry().distance(command.getLink2().getGeometry())) {

				rr.setLinkPid(command.getLink1().getPid());
			}else{
				rr.setLinkPid(command.getLink2().getPid());
			}

			changedFields.put("linkPid", inLinkPid);

			result.insertObject(rr, ObjStatus.UPDATE, rr.pid());

		}
	}

}
