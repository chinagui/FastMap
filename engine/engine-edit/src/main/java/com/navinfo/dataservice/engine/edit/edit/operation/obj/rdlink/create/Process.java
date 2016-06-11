package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.create;

import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {
		super.preCheck();
		
		check.checkDupilicateNode(this.getCommand().getGeometry());
		
		check.checkGLM04002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
		
		check.checkGLM13002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
		
		return null;
	}

	@Override
	public void postCheck() throws Exception {
		super.postCheck();
		check.postCheck(this.getConn(), this.getResult(),this.getCommand().getProjectId());
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		return msg;
	}
	
}
