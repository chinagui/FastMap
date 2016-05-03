package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(Command command) throws Exception {
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
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), updateLink);
	}


}
