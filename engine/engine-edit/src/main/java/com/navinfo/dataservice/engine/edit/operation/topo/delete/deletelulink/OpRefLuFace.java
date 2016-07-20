package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;

public class OpRefLuFace implements IOperation {

	private Command command;

	private Result result;

	public OpRefLuFace(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		this.result = result;

		if (null != command.getFaces()) {
			for (LuFace face : command.getFaces()) {
				result.insertObject(face, ObjStatus.DELETE, face.getPid());
			}
		}
		
		return null;
	}

}
