package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete;

import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		this.getCommand().setLanes(
				new RdLaneSelector(this.getConn()).loadByLink(this.getCommand()
						.getLinkPid(), this.getCommand().getLaneDir(), true));
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
