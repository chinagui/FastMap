package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		this.getCommand().setUpdateLink(
				(LcLink) new LcLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		this.getCommand()
				.setFaces(new LcFaceSelector(this.getConn()).loadLcFaceByLinkId(this.getCommand().getLinkPid(), true));
		

		// 查询需要修行的线上是否存在立交
		RdGscSelector gscSelector = new RdGscSelector(this.getConn());

		List<RdGsc> gscList = gscSelector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "LC_LINK", true);

		this.getCommand().setGscList(gscList);

		return false;
	}

	@Override
	public String preCheck() throws Exception {
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		RdGscOperateUtils.checkIsMoveGscPoint(this.getCommand().getLinkGeom(), this.getConn(), this.getCommand().getLinkPid(),"LC_LINK");
		return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
	}

}
