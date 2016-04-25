package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;

public class Operation implements IOperation {

	private Command command;

	private AdAdmin adAdmin;

	public Operation(Command command, AdAdmin adAdmin) {
		this.command = command;

		this.adAdmin = adAdmin;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(adAdmin, ObjStatus.DELETE, adAdmin.pid());
				
		return null;
	}

}
