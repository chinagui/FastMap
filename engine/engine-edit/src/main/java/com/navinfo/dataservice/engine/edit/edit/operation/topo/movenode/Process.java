package com.navinfo.dataservice.engine.edit.edit.operation.topo.movenode;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	private RdNode updateNode;
	
	
	public void lockRdLink() throws Exception {

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		List<RdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		
		List<Integer> linkPids = new ArrayList<Integer>();
		
		for(RdLink link : links){
			linkPids.add(link.getPid());
		}

		this.getCommand().setLinks(links);
	}
	
	@Override
	public boolean prepareData() throws Exception {
		RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());
		
		this.updateNode = (RdNode) nodeSelector.loadById(this.getCommand().getNodePid(), true);
		
		lockRdLink();
		
		return false;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),updateNode);
	}

}
