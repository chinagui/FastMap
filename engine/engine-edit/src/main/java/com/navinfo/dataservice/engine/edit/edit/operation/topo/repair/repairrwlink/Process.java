package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrwlink;

import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
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
		
		this.getCommand().setUpdateLink((RwLink) new RwLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		
		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
