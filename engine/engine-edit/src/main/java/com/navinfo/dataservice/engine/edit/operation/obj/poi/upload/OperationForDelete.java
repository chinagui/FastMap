package com.navinfo.dataservice.engine.edit.operation.obj.poi.upload;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

public class OperationForDelete implements IOperation {

	private CommandForDelete command;

	private IxPoi ixPoi;

	public OperationForDelete(CommandForDelete command, IxPoi ixPoi) {
		this.command = command;

		this.ixPoi = ixPoi;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(ixPoi, ObjStatus.DELETE, command.getPid());

		return null;
	}
}
