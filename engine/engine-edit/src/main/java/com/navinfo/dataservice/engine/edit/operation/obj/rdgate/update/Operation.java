package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;

import net.sf.json.JSONObject;

public class Operation implements IOperation {
	
	private Command command;
	
	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		try {
			updateRdGate(result);
		} catch (Exception e) {
			throw e;
		}
		return null;
	}
	
	public void updateRdGate(Result result) throws Exception {
		JSONObject content = command.getContent();

		RdGate rdGate = command.getRdGate();

		boolean isChanged = rdGate.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(rdGate, ObjStatus.UPDATE, rdGate.pid());
			result.setPrimaryPid(rdGate.pid());
		}
	}

}
