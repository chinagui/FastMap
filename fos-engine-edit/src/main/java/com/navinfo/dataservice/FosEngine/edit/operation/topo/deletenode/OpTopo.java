package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletenode;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
		}
		
		for(RdLink link : command.getLinks()){
			
			result.insertObject(link, ObjStatus.DELETE);
		}
		
		return msg;
	}

}
