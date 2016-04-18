package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadlink;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;

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

		for (AdFace adFace : list) {
			result.insertObject(adFace, ObjStatus.DELETE,adFace.getPid());
		}
	}

	
}
