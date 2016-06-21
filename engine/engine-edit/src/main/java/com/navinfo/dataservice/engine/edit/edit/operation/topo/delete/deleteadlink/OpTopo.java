package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleteadlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;

public class OpTopo implements IOperation {

	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	
	@Override
	public String run(Result result) {
		
		
		String msg = null;
		
		AdLink link = command.getLink();
		
		result.setPrimaryPid(link.getPid());
		
		result.insertObject(link, ObjStatus.DELETE, link.pid());
		
		for(AdNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE, node.pid());
		}
		
		return msg;
	}

}
