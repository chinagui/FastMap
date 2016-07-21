package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Command;
import com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Operation;

public class Process extends  AbstractProcess<Command> implements IProcess{
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
	}
}
