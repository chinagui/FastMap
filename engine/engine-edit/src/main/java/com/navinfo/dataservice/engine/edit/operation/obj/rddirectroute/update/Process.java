package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update;

import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdDirectrouteSelector selector = new RdDirectrouteSelector(
				this.getConn());

		RdDirectroute directroute = (RdDirectroute) selector.loadById(this
				.getCommand().getPid(), true);

		this.getCommand().setDirectroute(directroute);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}