package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

public class OpRefRdGsc implements IOperation {

	private Command command;

	public OpRefRdGsc(Command command, Connection connection) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {


		List<RdGsc> rdGscList = command.getRdGscs();

		if (CollectionUtils.isNotEmpty(rdGscList)) {
			com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation updateOp = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation("RD_LINK");
			
			updateOp.breakLineForGsc(result, command.getBreakLink(), command.getNewLinks(), rdGscList);
		}

		return null;
	}

}
