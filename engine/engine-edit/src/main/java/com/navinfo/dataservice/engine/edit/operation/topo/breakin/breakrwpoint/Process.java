package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

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
		super();
		this.setCommand(command);
		// 初始化检查参数
		this.initCheckCommand();
		this.setConn(conn);
		this.setResult(result);
	}

	public boolean prepareData() throws Exception {
		// 获取由该link组成的立交（RDGSC）
		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RW_LINK", true);

		this.getCommand().setRdGscs(rdGscList);

		// 获取要打断LCLINK的对象
		RwLink breakLink = (RwLink) new RwLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true,
				false);
		this.getCommand().setBreakLink(breakLink);
		// 删除要打断LCLINK
		this.getResult().insertObject(breakLink, ObjStatus.DELETE, breakLink.pid());

		return true;

	}

	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();

			// 创建铁路点有关铁路线具体操作
			OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
			msg = operation.run(this.getResult());
			// 打断线对立交影响
			OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand());
			opRefRdGsc.run(this.getResult());
			
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

	@Override
	public String exeOperation() throws Exception {
		// 创建铁路点有关行政区划线具体操作
		OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		// 打断线对立交影响
		OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand());
		opRefRdGsc.run(this.getResult());
		return msg;
	}

}
