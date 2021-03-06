package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint.OpRefRdGsc;

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
		// 获取由该link组成的立交（RDGSC）
		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this
				.getCommand().getLinkPid(), "LC_LINK", true);

		this.getCommand().setRdGscs(rdGscList);

		return true;
	}

	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();

			// 创建土地覆盖点有关土地覆盖线具体操作
			OpTopo operation = new OpTopo(this.getCommand());
			msg = operation.run(this.getResult());
			// 创建土地覆盖点有关土地覆盖面具体操作类
			OpRefLcFace opRefLcFace = new OpRefLcFace(this.getCommand(),
					this.getConn());
			opRefLcFace.run(this.getResult());

			// 打断线对立交影响
			OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand());
			opRefRdGsc.run(this.getResult());

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

		// 打断线对立交影响
		OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.getCommand());
		opRefRdGsc.run(this.getResult());
		return msg;
	}

}
