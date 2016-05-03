package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command>  implements IProcess {
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.Abstractprocess#createOperation()
	 */
	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.getConn());
	}

}
