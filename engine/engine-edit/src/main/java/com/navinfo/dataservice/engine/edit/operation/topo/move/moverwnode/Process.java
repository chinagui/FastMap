package com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}

	@Override
	public boolean prepareData() throws Exception {

		if (this.getCommand().getUpdateNode() == null) {
			RwNodeSelector nodeSelector = new RwNodeSelector(this.getConn());

			RwNode updateNode = (RwNode) nodeSelector.GetRwNodeWithLinkById(
					this.getCommand().getNodePid(), true);
			this.getCommand().setUpdateNode(updateNode);
			this.getCommand().setLinks(updateNode.getTopoLinks());
		}

		return false;
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand(),
					this.getConn());

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}

}
