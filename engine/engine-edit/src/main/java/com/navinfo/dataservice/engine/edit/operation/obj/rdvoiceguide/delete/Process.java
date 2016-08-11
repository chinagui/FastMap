package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete;

import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private RdVoiceguide voiceguide;

	@Override
	public boolean prepareData() throws Exception {

		AbstractSelector selector = new AbstractSelector(this.getConn());

		this.voiceguide = (RdVoiceguide) selector.loadById(this.getCommand()
				.getPid(), true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {

		return new Operation(this.getCommand(), this.voiceguide).run(this
				.getResult());
	}
}