package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
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
		
		for(RdNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE);
			
			result.setPrimaryPid(node.getPid());
		}
		
		for(RdLink link : command.getLinks()){
			
			result.insertObject(link, ObjStatus.DELETE);
		}
		
		return msg;
	}

}
