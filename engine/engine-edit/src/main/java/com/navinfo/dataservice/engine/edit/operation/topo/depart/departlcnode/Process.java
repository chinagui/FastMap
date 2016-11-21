package com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;

import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.depart.departlcnode.Operation;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public boolean prepareData() throws Exception {
		LcLinkSelector linkSelector = new LcLinkSelector(this.getConn());

		LcNodeSelector nodeSelector = new LcNodeSelector(this.getConn());
		// 加载分离LcLink信息
		LcLink link = (LcLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);
		// 加载LcLNode挂接的LcLLink信息
		List<LcLink> links = linkSelector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		// 加载挂接的LcLNode信息
		LcNode node = (LcNode) nodeSelector.loadById(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
		this.getCommand().setLcLink(link);
		this.getCommand().setNode(node);
		return true;
	}

	@Override
	public String preCheck() throws Exception {
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
