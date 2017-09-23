package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create.Operation;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create.Command;;;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
