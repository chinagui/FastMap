package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete;

import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		AbstractSelector selector = new AbstractSelector(RdSameLink.class,
				this.getConn());

		RdSameLink rdSameLink = (RdSameLink) selector.loadById(this
				.getCommand().getPid(), true);

		this.getCommand().setRdSameLink(rdSameLink);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		
		Operation operation = new Operation(this.getCommand());

		String msg = operation.run(this.getResult());

		return msg;

	}
}