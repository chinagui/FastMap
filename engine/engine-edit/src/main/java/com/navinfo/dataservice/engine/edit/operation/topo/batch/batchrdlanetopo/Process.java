package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopo;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	
	@Override
	public boolean prepareData() throws Exception {
		return true;
	}
	@Override
	public String preCheck() throws Exception {
		
		return null;
	}


	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
	
}
