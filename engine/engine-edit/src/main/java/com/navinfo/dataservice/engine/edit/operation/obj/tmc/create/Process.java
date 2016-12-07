package com.navinfo.dataservice.engine.edit.operation.obj.tmc.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}
}
