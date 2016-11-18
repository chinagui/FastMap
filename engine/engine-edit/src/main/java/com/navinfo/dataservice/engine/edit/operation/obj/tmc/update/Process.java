package com.navinfo.dataservice.engine.edit.operation.obj.tmc.update;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		AbstractSelector selector = new AbstractSelector(RdRoad.class,
				this.getConn());

		RdTmclocation location = (RdTmclocation) selector.loadById(this.getCommand().getPid(),
				true);

		this.getCommand().setRdTmclocation(location);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {

		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
