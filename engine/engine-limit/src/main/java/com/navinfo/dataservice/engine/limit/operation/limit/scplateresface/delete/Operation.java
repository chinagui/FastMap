package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;


public class Operation implements IOperation{

	private Command command;
	
	public Operation(Command command){
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {
		
		for(ScPlateresFace scface: this.command.getscplateresFaces()){
			result.insertObject(scface, ObjStatus.DELETE, scface .getGeometryId());
		}
		return null;
	}

}
