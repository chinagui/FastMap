package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class OpRefRestrict implements IOperation {
	
	private Command command;
	
	private Connection conn;
	
	public OpRefRestrict(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;
	}
	
	@Override
	public String run(Result result) throws Exception {

		List<Integer> linkPidList = new ArrayList<>();
		
		linkPidList.add(this.command.getLinkPid());
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation resDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation(this.conn);
		
		resDelOption.deleteRdRestrictionByLink(linkPidList, result);
		
		return null;
	}
}
