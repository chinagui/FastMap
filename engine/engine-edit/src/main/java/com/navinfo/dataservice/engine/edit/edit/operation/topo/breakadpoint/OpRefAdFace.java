package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;

/**
 * @author zhaokk
 * 创建行政区划点有关行政区划面具体操作类
 *
 */
public class OpRefAdFace implements IOperation {

	private Command command;

	private Result result;

	public OpRefAdFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleAdFaceTopo(command.getAdFaceTopos());
		
		return null;
	}

	/*
	 *  @author zhaokk
	 *  @param List
	 *  修改AdFace 和AdLink topo 关系
	 *  
	 */
	private void handleAdFaceTopo(List<AdFaceTopo> list)
			throws Exception {
		for (AdFaceTopo adFaceTopo : list) {
			result.insertObject(adFaceTopo, ObjStatus.DELETE,adFaceTopo.getFacePid());
			adFaceTopo.setLinkPid(command.getsAdLink().getPid());
			result.insertObject(adFaceTopo, ObjStatus.INSERT,adFaceTopo.getFacePid());
			adFaceTopo.setLinkPid(command.geteAdLink().getPid());
			result.insertObject(adFaceTopo, ObjStatus.INSERT,adFaceTopo.getFacePid());
		}
	}

	
}
