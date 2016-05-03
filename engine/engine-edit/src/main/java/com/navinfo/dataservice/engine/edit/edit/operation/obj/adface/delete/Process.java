package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	private Check check = new Check();
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void postCheck() throws Exception {
		
		check.postCheck(this.getConn(), this.getResult());
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public IOperation createOperation() {
		return new Operation(this.getCommand(), check, this.getConn());
	}


}
