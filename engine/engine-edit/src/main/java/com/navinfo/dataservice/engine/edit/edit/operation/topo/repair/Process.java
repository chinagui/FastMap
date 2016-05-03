package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair;

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
	
	private RdLink updateLink;
	
	private RdNode snode;
	
	private RdNode enode;
	
	private Check check = new Check();

	@Override
	public boolean prepareData() throws Exception {
		
		this.updateLink = (RdLink) new RdLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true);
		
		RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());
		
		this.snode = (RdNode) nodeSelector.loadById(updateLink.getsNodePid(), true);
		
		this.enode = (RdNode) nodeSelector.loadById(updateLink.geteNodePid(), true);
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
		
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		
		return null;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getConn(), this.getCommand(),updateLink,snode,enode,check);
	}

}
