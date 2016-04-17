package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

public class Operation implements IOperation {

	private Command command;

	private RdGsc rdGsc;

	public Operation(Command command, RdGsc rdGsc) {
		this.command = command;

		this.rdGsc = rdGsc;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(rdGsc);
				
		return null;
	}

}
