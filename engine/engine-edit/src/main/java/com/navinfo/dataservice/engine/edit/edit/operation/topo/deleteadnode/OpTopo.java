package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadnode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;

public class OpTopo implements IOperation {

	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	
	@Override
	public String run(Result result) {
		
		
		String msg = null;
		
		for(AdNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE, node.pid());
			
			result.setPrimaryPid(node.getPid());
		}
		
		for(AdLink link : command.getLinks()){
			
			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}
		
		return msg;
	}

}
