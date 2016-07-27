package com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode;

import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {
		RwNodeSelector nodeSelector = new RwNodeSelector(this.getConn());

		RwNode updateNode = (RwNode) nodeSelector.GetRwNodeWithLinkById(this
				.getCommand().getNodePid(), true);

		this.getCommand().setUpdateNode(updateNode);

		this.getCommand().setLinks(updateNode.getTopoLinks());

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this
				.getResult());
	}

}
