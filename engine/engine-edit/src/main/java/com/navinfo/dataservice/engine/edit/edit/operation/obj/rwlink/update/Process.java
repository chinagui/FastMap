package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.update;

import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private RwLink updateLink;

	@Override
	public boolean prepareData() throws Exception {
		
		RwLinkSelector linkSelector = new RwLinkSelector(this.getConn());

		this.updateLink = (RwLink) linkSelector.loadById(this.getCommand().getLinkPid(),
				true);

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateLink).run(this.getResult());
	}


}
