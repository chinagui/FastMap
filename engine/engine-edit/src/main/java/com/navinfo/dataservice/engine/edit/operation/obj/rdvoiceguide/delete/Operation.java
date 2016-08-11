package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;

public class Operation implements IOperation {

	private Command command;

	private RdVoiceguide voiceguide;

	public Operation(Command command, RdVoiceguide voiceguide) {
		this.command = command;

		this.voiceguide = voiceguide;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = delete(result);

		return msg;
	}

	private String delete(Result result) {

		result.insertObject(voiceguide, ObjStatus.DELETE, voiceguide.pid());

		return null;
	}
}
