package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail;

import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	
	@Override
	public boolean prepareData() throws Exception {
		this.getCommand().setDelToptInfos(new RdLaneTopoDetailSelector(this.getConn()).loadByIds(this.getCommand().getTopoIds(), true, true));
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
