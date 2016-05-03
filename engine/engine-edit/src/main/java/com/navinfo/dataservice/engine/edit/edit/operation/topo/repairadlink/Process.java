package com.navinfo.dataservice.engine.edit.edit.operation.topo.repairadlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	private AdLink updateLink;
	
	private AdNode snode;
	
	private AdNode enode;
	
	private Check check = new Check();
	
	public Process(Command command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public boolean prepareData() throws Exception {
		
		this.updateLink = (AdLink) new AdLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true);
		
		AdNodeSelector nodeSelector = new AdNodeSelector(this.getConn());
		
		this.snode = (AdNode) nodeSelector.loadById(updateLink.geteNodePid(), true);
		
		this.enode = (AdNode) nodeSelector.loadById(updateLink.getsNodePid(), true);
		
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
