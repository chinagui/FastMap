package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrdlink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check();

	@Override
	public boolean prepareData() throws Exception {

		int linkPid = this.getCommand().getLinkPid();

		this.getCommand().setUpdateLink((RdLink) new RdLinkSelector(this.getConn()).loadById(linkPid, true));

		// 查询需要修行的线上是否存在立交
		RdGscSelector gscSelector = new RdGscSelector(this.getConn());

		List<RdGsc> gscList = gscSelector.onlyLoadRdGscLinkByLinkPid(linkPid, "RD_LINK", true);

		this.getCommand().setGscList(gscList);

		return false;
	}

	@Override
	public String preCheck() throws Exception {
		super.preCheck();
		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());

		check.checkShapePointDistance(this.getCommand().getLinkGeom());

		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		check.checkIsMoveGscPoint(this.getCommand().getLinkGeom(), this.getConn(), this.getCommand().getLinkPid());
		return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
	}

}
