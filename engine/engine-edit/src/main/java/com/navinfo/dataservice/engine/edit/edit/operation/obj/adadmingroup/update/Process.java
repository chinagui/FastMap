package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.Abstractprocess#createOperation(com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand, java.sql.Connection)
	 */
	@Override
	public IOperation createOperation() {
		return new Operation(this.getCommand(), this.getConn());
	}

}
