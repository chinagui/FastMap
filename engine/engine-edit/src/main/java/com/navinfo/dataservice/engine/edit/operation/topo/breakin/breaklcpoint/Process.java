package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		// 初始化检查参数
		this.initCheckCommand();
		this.setConn(conn);
		this.setResult(result);
	}

	public boolean prepareData() throws Exception {
		// 获取此LCLINK上土地覆盖面拓扑关系
		List<LcFace> faces = new LcFaceSelector(this.getConn())
				.loadLcFaceByLinkId(this.getCommand().getLinkPid(), true);
		this.getCommand().setFaces(faces);
		// 获取要打断LCLINK的对象
		LcLink breakLink = (LcLink) new LcLinkSelector(this.getConn())
				.loadById(this.getCommand().getLinkPid(), true, false);
		this.getCommand().setBreakLink(breakLink);
		// 删除要打断LCLINK
		this.getResult().insertObject(breakLink, ObjStatus.DELETE,
				breakLink.pid());

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
			// 创建土地覆盖点有关土地覆盖线具体操作
			OpTopo operation = new OpTopo(this.getCommand());
			msg = operation.run(this.getResult());
			// 创建土地覆盖点有关土地覆盖面具体操作类
			OpRefLcFace opRefLcFace = new OpRefLcFace(this.getCommand(),
					this.getConn());
			opRefLcFace.run(this.getResult());
		} catch (Exception e) {
			this.getConn().rollback();
			throw e;
		}
		return msg;
	}

	@Override
	public void postCheck() throws Exception {
		check.postCheck(this.getConn(), this.getResult(), this.getCommand()
				.getDbId());
		super.postCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		// 创建土地覆盖点有关土地覆盖线具体操作
		OpTopo operation = new OpTopo(this.getCommand());
		String msg = operation.run(this.getResult());
		// 创建土地覆盖点有关土地覆盖面具体操作类
		OpRefLcFace opRefLcFace = new OpRefLcFace(this.getCommand(),
				this.getConn());
		opRefLcFace.run(this.getResult());
		return msg;
	}

}
