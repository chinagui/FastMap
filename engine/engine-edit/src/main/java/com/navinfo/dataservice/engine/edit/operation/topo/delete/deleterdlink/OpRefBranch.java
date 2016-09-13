package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;

public class OpRefBranch implements IOperation {
	
	private Command command;

	public OpRefBranch(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdBranch branch : command.getBranches()){
			
			result.insertObject(branch, ObjStatus.DELETE, branch.pid());
		}
		
		return null;
	}
	
	/**
	 * 删除进入link对分歧的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteBranchInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : command.getInLinkBranchs()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除退出link对分歧的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteBOutLinkranchInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdBranch branch : command.getOutLinkDeleteBranchs()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(branch.objType());

			alertObj.setPid(branch.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
