package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;

public class OpRefAdAdmin implements IOperation {
	
	private Command command;

	public OpRefAdAdmin(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( AdAdmin adAdmin : command.getAdAdmins()){
			
			adAdmin.changedFields().put("linkPid", 0);
			
			result.insertObject(adAdmin, ObjStatus.UPDATE, adAdmin.pid());
		}
		
		return null;
	}

}
