package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	Check check = new Check();
	
	@Override
	public String exeOperation() throws Exception {
		check.hasMakedCRFI(this.getCommand(), this.getConn());
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}
}
