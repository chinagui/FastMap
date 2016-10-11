package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {
		check.checkGeometryNoOnMeshBoarder(this.getCommand().getLongitude(), this.getCommand().getLatitude());
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
