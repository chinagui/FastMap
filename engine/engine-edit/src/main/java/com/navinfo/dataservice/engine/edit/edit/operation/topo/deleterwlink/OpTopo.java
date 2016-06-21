package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleterwlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;

public class OpTopo implements IOperation {

	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	
	@Override
	public String run(Result result) {
		
		String msg = null;
		
		RwLink link = command.getLink();
		
		result.setPrimaryPid(link.getPid());
		
		result.insertObject(link, ObjStatus.DELETE, link.pid());
		
		for(RwNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE, node.pid());
		}
		
		return msg;
	}

}
