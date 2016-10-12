package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String preCheck() throws Exception {
		Command command = this.getCommand();
		check.checkGeometryNoOnMeshBoarder(command.getLongitude(), command.getLatitude());
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
