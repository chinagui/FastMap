package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.update;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdLink updateLink;

	@Override
	public boolean prepareData() throws Exception {
		
		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

		this.updateLink = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(),
				true);

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateLink).run(this.getResult());
	}


}
