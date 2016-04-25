package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadlink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;

public class OpRefAdFace implements IOperation {

	private Command command;

	private Result result;

	public OpRefAdFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleAdFace(command.getFaces());
		
		return null;
	}

	// 处理面
	private void handleAdFace(List<AdFace> list)throws Exception {
		if(list != null && list.size() > 0){
		for (AdFace adFace : list) {
			result.insertObject(adFace, ObjStatus.DELETE,adFace.getPid());
		 }
		}
	}

	
}
