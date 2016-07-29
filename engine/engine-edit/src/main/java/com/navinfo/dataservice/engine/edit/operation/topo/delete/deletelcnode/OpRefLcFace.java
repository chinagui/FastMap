package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;


public class OpRefLcFace implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	public OpRefLcFace(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;
		for(LcFace face : command.getFaces()){
			result.insertObject(face, ObjStatus.DELETE, face.pid());
			result.setPrimaryPid(face.getPid());
		}
		return msg;
	}
}

	

