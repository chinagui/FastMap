package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
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
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}

}
