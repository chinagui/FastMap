package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deletezonenode;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;

/**
 * @author  zhaokk 
 * 删除ZONE点对应AD_LINK和AD_NDE
 */
public class OpTopo implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;
	
	public OpTopo(Command command){
		
		this.command=command;
	}
	/*
	 * @author  zhaokk 
	 * 删除ZONE点对应
	 * ZONE_LINK ZONE_NODE
	 */
	@Override
	public String run(Result result) {
		
		
		String msg = null;
		log.info("1.删除ZONE点对应的点关系");
		result.insertObject(command.getNode(), ObjStatus.DELETE, command.getNode().pid());
		for(ZoneNode node : command.getNodes()){
			
			result.insertObject(node, ObjStatus.DELETE, node.pid());
			
			result.setPrimaryPid(node.getPid());
		}
		log.info("2.删除ZONE点对应的线关系");
		for(ZoneLink link : command.getLinks()){
			
			result.insertObject(link, ObjStatus.DELETE, link.pid());
		}
		
		return msg;
	}

}
