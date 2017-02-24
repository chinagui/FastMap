package com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode;

import java.util.List;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.parameterCheck.DepartCheck;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public boolean prepareData() throws Exception {

		AdLinkSelector linkSelector = new AdLinkSelector(this.getConn());

		AdNodeSelector nodeSelector = new AdNodeSelector(this.getConn());
		// 加载分离AdLink信息
		AdLink link = (AdLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);
		// 加载AdNode挂接的AdLink信息
		List<AdLink> links = linkSelector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		// 加载挂接的AdNode信息
		AdNode node = (AdNode) nodeSelector.loadById(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
		this.getCommand().setAdLink(link);
		this.getCommand().setNode(node);
		return true;
	}

	@Override
	public String preCheck() throws Exception {
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		
		parameterCheck();
		
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

	private void parameterCheck() throws Exception {
		DepartCheck departCheck = new DepartCheck(this.getConn());

		departCheck.checkIsSameNode(this.getCommand().getNodePid(), "AD_NODE");
	}

}
