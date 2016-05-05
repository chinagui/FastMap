package com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private AdLink updateLink;

	private Check check = new Check();

	public Process(Command command) throws Exception {
		super(command);

	}
	
	public Process(Command command, Connection conn) throws Exception  {
		super(command);
		this.setCommand(command);
		this.setResult(new Result());
		this.setConn(conn);
	}


	@Override
	public boolean prepareData() throws Exception {

		AdLinkSelector linkSelector = new AdLinkSelector(this.getConn());
		this.updateLink = (AdLink) linkSelector.loadById(this.getCommand().getLinkPid(),true);

		return true;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkIsCrossNode(this.getConn(), this.getCommand().getsNodePid());

		check.checkIsCrossNode(this.getConn(), this.getCommand().geteNodePid());

		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());

		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), updateLink, check).run(this.getResult());
	}
	
}
