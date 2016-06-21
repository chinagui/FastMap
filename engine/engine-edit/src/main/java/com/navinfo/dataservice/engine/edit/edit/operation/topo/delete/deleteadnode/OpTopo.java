package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleteadnode;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;

/**
 * @author  zhaokk 
 * 删除行政区划点对应AD_LINK和AD_NDE
 */
public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	/*
	 * @author  zhaokk 
	 * 删除行政区划点对应
	 * AD_LINKAD_NDE
	 */
	@Override
	public String run(Result result) {
		
		
		String msg = null;
		log.info("1.删除行政区划点对应的点关系");
		result.insertObject(command.getNode(), ObjStatus.DELETE, command.getNode().pid());
		for(AdNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE, node.pid());
			
			result.setPrimaryPid(node.getPid());
		}
		log.info("2.删除行政区划点对应的线关系");
		for(AdLink link : command.getLinks()){
			
			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}
		
		return msg;
	}

}
