package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairadlink;

import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.operator.ad.geo.AdFaceOperator;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public boolean prepareData() throws Exception {
		
		this.getCommand().setUpdateLink((AdLink) new AdLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		
		this.getCommand().setFaces(new AdFaceSelector(this.getConn()).loadAdFaceByLinkId(this.getCommand().getLinkPid(), true));
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
		
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		
		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
	}

}
