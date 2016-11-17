package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create;

import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	/*
	 * 加载主路
	 */
	public void lockRdLinks() throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());
		this.getCommand().setLinks(linkSelector.loadByPids(this.getCommand().getLinkPids(), true));
	}
	@Override
	public boolean prepareData() throws Exception {
		lockRdLinks();
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}