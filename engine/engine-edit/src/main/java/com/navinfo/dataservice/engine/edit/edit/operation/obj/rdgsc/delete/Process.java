package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private RdGsc rdGsc;

	public Process(Command command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn) throws Exception {
		super(command);

		this.setResult(result);

		this.setConn(conn);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		this.rdGsc = (RdGsc) selector.loadById(this.getCommand().getPid(), true);

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

			IOperation operation = new Operation(this.getCommand(), this.rdGsc);

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return  new Operation(this.getCommand(), this.rdGsc);
	}

}
