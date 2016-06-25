package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
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
		// 获取由该link组成的立交（RDGSC）
		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RW_LINK", true);

		this.getCommand().setRdGscs(rdGscList);
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
		// 打断线对立交影响
		OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
		opRefRdGsc.run(this.getResult());
		return msg;
	}

}
