package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpTopo implements IOperation {

	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	
	@Override
	public String run(Result result) {
		
		
		String msg = null;
		
		RdLink link = command.getLink();
		
		result.insertObject(link, ObjStatus.DELETE);
		
		for(RdNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE);
		}
		
		return msg;
	}

}
