package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;

import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super(command);
		this.setResult(result);
		this.setConn(conn);

	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {
		RdSlope slope = (RdSlope) new RdSlopeSelector(this.getConn()).loadById(
				this.getCommand().getPid(), true);
		this.getCommand().setSlope(slope);
		return true;
	}
}
