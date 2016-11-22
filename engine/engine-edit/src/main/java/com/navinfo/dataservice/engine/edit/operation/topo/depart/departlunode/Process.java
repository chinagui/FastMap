package com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.depart.departlunode.Operation;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public boolean prepareData() throws Exception {
		LuLinkSelector linkSelector = new LuLinkSelector(this.getConn());

		LuNodeSelector nodeSelector = new LuNodeSelector(this.getConn());
		// 加载分离LuLink信息
		LuLink link = (LuLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);
		// 加载LuNode挂接的LuLink信息
		List<LuLink> links = linkSelector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		// 加载挂接的LuNode信息
		LuNode node = (LuNode) nodeSelector.loadById(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
		this.getCommand().setLuLink(link);
		this.getCommand().setNode(node);
		return true;
	}

	@Override
	public String preCheck() throws Exception {
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
