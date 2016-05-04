package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command>{
	private Check check = new Check();
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), check, this.getConn());
	}

}
