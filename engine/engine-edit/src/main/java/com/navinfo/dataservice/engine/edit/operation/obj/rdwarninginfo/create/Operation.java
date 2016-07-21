package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.pidservice.PidService;
public class Operation implements IOperation{

	private Command command;

	public Operation(Command command) {
		
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {
		
		RdWarninginfo warninginfo = new RdWarninginfo();
		
		String msg = null;		
		
		warninginfo.setPid(PidService.getInstance().applyRdWarninginfoPid());
		
		result.setPrimaryPid(warninginfo.getPid());		
		
		warninginfo.setNodePid(command.getNodePid());

		warninginfo.setLinkPid(command.getLinkPid());
		
		result.insertObject(warninginfo, ObjStatus.INSERT, warninginfo.getPid());
		
		return msg;
	}

}
