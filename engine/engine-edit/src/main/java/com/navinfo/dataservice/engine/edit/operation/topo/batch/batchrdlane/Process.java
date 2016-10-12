package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		List<IRow> links = new RdLinkSelector(this.getConn()).loadByIds(this
				.getCommand().getLinkPids(), true, false);
		this.getCommand().setLinks(links);
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
