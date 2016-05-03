package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private Check check = new Check();

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.getConn());
	}
	
}
