package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink;

import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Check;

public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		
		this.getCommand().setUpdateLink((LuLink) new LuLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		
		this.getCommand().setFaces(new LuFaceSelector(this.getConn()).loadLuFaceByLinkId(this.getCommand().getLinkPid(), true));
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
		return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
	}

}
