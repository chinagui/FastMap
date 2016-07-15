package com.navinfo.dataservice.engine.edit.operation.obj.luface.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	private Check check;
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	
	@Override
	public void postCheck() throws Exception {
		
		check.postCheck(this.getConn(), this.getResult(), this.getCommand().getDbId());
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), check, this.getConn()).run(this.getResult());
	}


}
