package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check(this.getCommand());

	@Override
	public String preCheck() throws Exception {
		super.preCheck();
		check.hasRdInter(getConn());

		check.checkLinkByNode(getConn());

		check.checkNodeDirect(getConn());
		return null;
	}

	@Override
	public boolean prepareData() throws Exception {
		return true;
	}

	@Override
	public void postCheck() throws Exception {
		super.postCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getCommand());
		String msg = operation.run(this.getResult());
		return msg;
	}

}
