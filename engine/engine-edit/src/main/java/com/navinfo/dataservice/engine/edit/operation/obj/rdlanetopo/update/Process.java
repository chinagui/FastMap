package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.update;

import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
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
		RdLaneTopoDetail detail = (RdLaneTopoDetail) new RdLaneTopoDetailSelector(this.getConn()).loadById(this
				.getCommand().getTopoId(), true);
		this.getCommand().setDetail(detail);
		return true;
	}
}
