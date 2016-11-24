package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update;

import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;
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
		RdSlope slope = (RdSlope) new RdSlopeSelector(this.getConn()).loadById(
				this.getCommand().getPid(), false);
		this.getCommand().setSlope(slope);
		return true;
	}
}
