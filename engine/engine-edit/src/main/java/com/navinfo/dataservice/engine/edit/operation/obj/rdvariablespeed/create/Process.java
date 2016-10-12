package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	private Check check = new Check(this.getCommand());
	
	@Override
	public boolean prepareData() throws Exception {
		check.hasRdVariableSpeed(getConn());
		return true;
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
	
}
