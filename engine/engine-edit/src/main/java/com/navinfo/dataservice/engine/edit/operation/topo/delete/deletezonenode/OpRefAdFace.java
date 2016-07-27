package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode;


import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
/**
 * 删除ZONE点对应删除FACE信息
 * @author zhaokk
 *
 */

public class OpRefAdFace implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;

	public OpRefAdFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;
		log.debug("删除ZONE点对应的面关系");
		for(ZoneFace face : command.getFaces()){
			
			result.insertObject(face, ObjStatus.DELETE, face.pid());
			
			result.setPrimaryPid(face.getPid());
		}
		return msg;
	}
}

	

