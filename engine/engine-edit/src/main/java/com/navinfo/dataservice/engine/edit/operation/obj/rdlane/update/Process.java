package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {
		RdLane lane = (RdLane) new RdLaneSelector(this.getConn()).loadById(this
				.getCommand().getLanePid(), true);
		this.getCommand().setRdLane(lane);
		return true;
	}
}
