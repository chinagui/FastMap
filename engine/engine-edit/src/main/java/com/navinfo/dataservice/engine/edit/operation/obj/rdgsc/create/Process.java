package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check(this.getConn());
	
	@Override
	public boolean prepareData() throws Exception {
		
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), check, this.getConn()).run(this.getResult());
	}
	
}
