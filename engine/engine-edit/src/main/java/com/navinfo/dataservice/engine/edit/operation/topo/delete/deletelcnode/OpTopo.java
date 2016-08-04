package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;

public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}

	@Override
	public String run(Result result) {
		String msg = null;
		result.insertObject(command.getNode(), ObjStatus.DELETE, command.getNode().pid());
		for(LcNode node : command.getNodes()){
			result.insertObject(node, ObjStatus.DELETE, node.pid());
			result.setPrimaryPid(node.getPid());
		}
		for(LcLink link : command.getLinks()){
			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}
		return msg;
	}

}
