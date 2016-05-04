package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadnode;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;


public class OpRefAdFace implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	private Result result;

	public OpRefAdFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;
		log.debug("删除行政区划点对应的面关系");
		for(AdFace face : command.getFaces()){
			
			result.insertObject(face, ObjStatus.DELETE, face.pid());
			
			result.setPrimaryPid(face.getPid());
		}
		return msg;
	}
}

	

