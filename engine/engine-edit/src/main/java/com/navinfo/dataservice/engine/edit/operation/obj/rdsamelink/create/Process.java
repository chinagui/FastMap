package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		return true;
	}

	@Override
	public String exeOperation() throws Exception {

		Operation operation = new Operation(this.getCommand(), getConn());

		String msg = operation.run(this.getResult());

		return msg;
	}

}
