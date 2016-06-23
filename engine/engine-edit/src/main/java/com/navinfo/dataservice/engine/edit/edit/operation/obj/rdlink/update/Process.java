package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	public Process(Command command, Result result, Connection conn) throws Exception {
		super(command);
		// this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);

	}

	private RdLink updateLink;

	@Override
	public boolean prepareData() throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

		this.updateLink = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateLink).run(this.getResult());
	}
	
	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand(), this.updateLink);

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}
}
