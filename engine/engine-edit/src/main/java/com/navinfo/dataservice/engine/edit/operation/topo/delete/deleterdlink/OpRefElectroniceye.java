package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;

public class OpRefElectroniceye implements IOperation {

	private Command command;

	public OpRefElectroniceye(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		for (RdElectroniceye eleceye : command.getElectroniceyes()) {
			eleceye.changedFields().put("linkPid", 0);
			result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
		}

		return null;
	}
}
