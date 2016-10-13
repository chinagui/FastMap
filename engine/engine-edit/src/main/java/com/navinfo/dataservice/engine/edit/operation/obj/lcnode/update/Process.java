package com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private LcNode lcnode;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn) throws Exception {
		super(command,result,conn);
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.lcnode).run(this.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {

		LcNodeSelector selector = new LcNodeSelector(this.getConn());

		this.lcnode = (LcNode) selector.loadById(this.getCommand().getPid(), true);

		return true;
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand(), this.lcnode);

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

}
