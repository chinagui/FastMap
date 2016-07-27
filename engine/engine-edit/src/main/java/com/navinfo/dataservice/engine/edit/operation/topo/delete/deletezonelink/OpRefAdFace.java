package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;

public class OpRefAdFace implements IOperation {

	private Command command;

	private Result result;

	public OpRefAdFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleZoneFace(command.getFaces());
		
		return null;
	}

	// 处理面
	private void handleZoneFace(List<ZoneFace> list)throws Exception {
		if(list != null && list.size() > 0){
		for (ZoneFace zoneFace : list) {
			result.insertObject(zoneFace, ObjStatus.DELETE,zoneFace.getPid());
		 }
		}
	}

	
}
