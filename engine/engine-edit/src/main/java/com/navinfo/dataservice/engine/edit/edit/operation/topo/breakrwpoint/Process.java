package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakrwpoint;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

/**
 * 新增铁路点具体执行类
 * 
 * @author zhangxiaolong
 */
public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn) throws Exception {
		super(command);
		this.setConn(conn);
		this.setResult(result);
	}

	public boolean prepareData() throws Exception {
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
			// 创建铁路点有关铁路线具体操作
			OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
			msg = operation.run(this.getResult());
			this.recordData();
		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	@Override
	public void postCheck() throws Exception {
		super.postCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		// 创建铁路点有关行政区划线具体操作
		OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		return msg;
	}

}
