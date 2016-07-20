package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;


public class OpRefLuFace implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	private Result result;

	public OpRefLuFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;
		// 删除土地利用点对应的面关系
		for(LuFace face : command.getFaces()){
			
			result.insertObject(face, ObjStatus.DELETE, face.pid());
			
			result.setPrimaryPid(face.getPid());
		}
		return msg;
	}
}

	

