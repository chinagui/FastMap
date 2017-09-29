package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.create;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}
	
}
