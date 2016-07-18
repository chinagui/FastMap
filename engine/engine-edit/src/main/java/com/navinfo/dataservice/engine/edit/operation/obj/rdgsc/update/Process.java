package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update;

import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private RdGsc rdGsc;

	@Override
	public boolean prepareData() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		this.rdGsc = (RdGsc) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), rdGsc).run(this.getResult());
	}

}
