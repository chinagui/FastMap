package com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;

import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode.Operation;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	@Override
	public boolean prepareData() throws Exception {
		ZoneLinkSelector linkSelector = new ZoneLinkSelector(this.getConn());

		ZoneNodeSelector nodeSelector = new ZoneNodeSelector(this.getConn());
		// 加载分离ZoneLink信息
		ZoneLink link = (ZoneLink) linkSelector.loadById(this.getCommand()
				.getLinkPid(), true);
		// 加载ZoneNode挂接的ZoneLink信息
		List<ZoneLink> links = linkSelector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		// 加载挂接的ZoneNode信息
		ZoneNode node = (ZoneNode) nodeSelector.loadById(this.getCommand()
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
