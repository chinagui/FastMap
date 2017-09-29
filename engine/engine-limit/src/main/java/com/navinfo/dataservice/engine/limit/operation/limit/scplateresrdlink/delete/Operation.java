package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;

public class Operation implements IOperation{

	private Command command;
	
	public Operation(Command command){
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {
		
		for(ScPlateresLink sclink: this.command.getscplateresLinks()){
			result.insertObject(sclink, ObjStatus.DELETE, sclink.getGeometryId());
		}
		return null;
	}

}
