package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink;

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

	@Override
	public boolean prepareData() throws Exception {
		this.getCommand().setUpdateLink(
				(LcLink) new LcLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		this.getCommand()
				.setFaces(new LcFaceSelector(this.getConn()).loadLcFaceByLinkId(this.getCommand().getLinkPid(), true));
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
	}

}
