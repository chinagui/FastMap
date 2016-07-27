package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {

		check.checkNoSameRelation(this.getConn(), this.getCommand().getInLinkPid(), this.getCommand().getNodePid());
		
		check.checkGLM08004(this.getConn(), this.getCommand().getInLinkPid(), this.getCommand().getOutLinkPids());
		
		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn(), check).run(this.getResult());
	}

	
	
}
