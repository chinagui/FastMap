package com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private LcLink updateLink;

	private Check check = new Check();

	public Process(Command command) throws Exception {
		super(command);

	}

	public Process(Command command, Connection conn) throws Exception {
		super(command);
		this.setCommand(command);
		this.setResult(new Result());
		this.setConn(conn);
	}

	@Override
	public boolean prepareData() throws Exception {
		LcLinkSelector linkSelector = new LcLinkSelector(this.getConn());
		this.updateLink = (LcLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);
		return true;
	}

	@Override
	public String preCheck() throws Exception {
		check.checkIsCrossNode(this.getConn(), this.getCommand().getsNodePid());
		check.checkIsCrossNode(this.getConn(), this.getCommand().geteNodePid());
		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateLink, check).run(this.getResult());
	}

}
