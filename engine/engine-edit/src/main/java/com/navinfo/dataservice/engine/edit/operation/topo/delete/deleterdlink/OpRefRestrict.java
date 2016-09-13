package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;

public class OpRefRestrict implements IOperation {
	
	private Command command;

	public OpRefRestrict(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdRestriction restrict : command.getRestrictions()){
			
			result.insertObject(restrict, ObjStatus.DELETE, restrict.pid());
		}
		
		return null;
	}
	
	/**
	 * 删除link对交限的更新影响分析
	 * @return
	 */
	public List<AlertObject> getUpdateResInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : command.getOutUpdateLinkRestrictions()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除进入link对交限的删除影响分析
	 * @return
	 */
	public List<AlertObject> getDeleteInLinkResInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : command.getInLinkRestrictions()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除退出link对交限的删除影响分析
	 * @return
	 */
	public List<AlertObject> getDeleteOutLinkResInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : command.getOutDeleteLinkRestrictions()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
