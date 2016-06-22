package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private Check check = new Check(this.getConn());
	
	@Override
	public boolean prepareData() throws Exception {
		
		return false;
	}



	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), check, this.getConn()).run(this.getResult());
	}
	
}
