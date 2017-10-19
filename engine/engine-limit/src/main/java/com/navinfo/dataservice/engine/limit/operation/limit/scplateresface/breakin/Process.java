package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.breakin;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;

public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception{
		ScPlateresFaceSearch search = new ScPlateresFaceSearch(this.getConn());
		
		ScPlateresFace face = search.loadById(this.getCommand().getGeometryId());
		
		this.getCommand().setFace(face);
		
		return true;
	}
	
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		Operation opration = new Operation(this.getCommand(),this.getConn());
		return opration.run(getResult());
	}

}
