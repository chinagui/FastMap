package com.navinfo.dataservice.engine.edit.operation.obj.lunode.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private LuNode lunode;

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super(command, result, conn);
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.lunode).run(this
				.getResult());
	}

	@Override
	public boolean prepareData() throws Exception {

		if (this.getCommand().getNode() != null) {
			this.lunode = this.getCommand().getNode();
			return true;
		}

		LuNodeSelector selector = new LuNodeSelector(this.getConn());

		this.lunode = (LuNode) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			IOperation operation = new Operation(this.getCommand(), this.lunode);

			msg = operation.run(this.getResult());

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}
}
