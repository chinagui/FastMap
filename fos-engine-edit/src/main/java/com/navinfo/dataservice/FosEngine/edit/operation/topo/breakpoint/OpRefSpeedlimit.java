package com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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

			changedFields.put("inLinkPid", inLinkPid);

			result.insertObject(rr, ObjStatus.UPDATE);

		}
	}

}
