package com.navinfo.dataservice.engine.edit.operation.obj.adnode.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	private AdNode adnode;

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
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.adnode).run(this
				.getResult());

	}

	@Override
	public boolean prepareData() throws Exception {
		if (this.getCommand().getNode() != null) {
			this.adnode = this.getCommand().getNode();
			return true;
		}

		AdNodeSelector selector = new AdNodeSelector(this.getConn());

		this.adnode = (AdNode) selector.loadById(this.getCommand().getPid(),
				true);

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

			IOperation operation = new Operation(this.getCommand(), this.adnode);

			msg = operation.run(this.getResult());

			this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	// @Override
	// public ICommand getCommand() {
	//
	// return command;
	// }
	//
	// @Override
	// public Result getResult() {
	//
	// return result;
	// }

	// @Override
	// public String preCheck() throws Exception {
	//
	// return null;
	// }

	// @Override
	// public String run() throws Exception {
	// String msg;
	// try {
	// conn.setAutoCommit(false);
	//
	// this.prepareData();
	//
	// String preCheckMsg = this.preCheck();
	//
	// if (preCheckMsg != null) {
	// throw new Exception(preCheckMsg);
	// }
	//
	// IOperation operation = new Operation(command, this.adnode);
	//
	// msg = operation.run(result);
	//
	// this.recordData();
	//
	// this.postCheck();
	//
	// conn.commit();
	//
	// } catch (Exception e) {
	//
	// conn.rollback();
	//
	// throw e;
	// } finally {
	// try {
	// conn.close();
	// } catch (Exception e) {
	//
	// }
	// }
	//
	// return msg;
	// }
	//

	// @Override
	// public void postCheck() throws Exception {
	//
	// }
	//
	// @Override
	// public String getPostCheck() throws Exception {
	//
	// return postCheckMsg;
	// }
	//
	// @Override
	// public boolean recordData() throws Exception {
	//
	// LogWriter lw = new LogWriter(conn, this.command.getDbId());
	//
	// lw.generateLog(command, result);
	//
	// OperatorFactory.recordData(conn, result);
	//
	// lw.recordLog(command, result);
	//
	// return true;
	// }

}
