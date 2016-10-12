package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdGsc implements IOperation {
	
	private Command command;
	
	private Connection conn;
	
	public OpRefRdGsc(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;
	}
	
	@Override
	public String run(Result result) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation gscDelOption = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Operation(
				this.conn);
		
		RdLink link = this.command.getLink();
		
		List<RdLink>  linkList = new ArrayList<>();
		
		linkList.add(link);
		
		gscDelOption.deleteByLinkPid(linkList, result);
		return null;
	}
}
