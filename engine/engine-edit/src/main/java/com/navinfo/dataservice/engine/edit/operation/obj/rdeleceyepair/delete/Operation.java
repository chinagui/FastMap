package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		deleteRdEleceyePair(result);
		return null;
	}

	public void deleteRdEleceyePair(Result result) {
		// 删除区间测速电子眼配对信息(同时删除子表信息)
		result.insertObject(command.getPair(), ObjStatus.DELETE, command.getPair().pid());
	}
	
	/**
	 * 删除link对电子眼的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteElectroniceyeInfectData(int linkPid,Connection conn) throws Exception {
		
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);

		List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdElectroniceye eye : eleceyes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(eye.objType());

			alertObj.setPid(eye.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
