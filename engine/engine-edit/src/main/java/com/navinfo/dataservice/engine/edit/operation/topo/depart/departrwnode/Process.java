package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode;

import java.util.List;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.depart.departrwnode.Operation;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public boolean prepareData() throws Exception {
		RwLinkSelector linkSelector = new RwLinkSelector(this.getConn());

		RwNodeSelector nodeSelector = new RwNodeSelector(this.getConn());
		// 加载分离RwLink信息
		RwLink link = (RwLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);
		// 加载RwNode挂接的RwLink信息
		List<RwLink> links = linkSelector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		// 加载挂接的RwNode信息
		RwNode node = (RwNode) nodeSelector.loadById(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
		this.getCommand().setRwLink(link);
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
