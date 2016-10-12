package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		delRelectroniceye(result);
		return null;
	}

	public void delRelectroniceye(Result result) {
		// 删除电子眼
		result.insertObject(this.command.getEleceye(), ObjStatus.DELETE, this.command.getEleceye().parentPKValue());
		// 删除电子眼组成关系表(同时删除子表信息)
		for (IRow pair : this.command.getEleceye().getPairs()) {
			result.insertObject(pair, ObjStatus.DELETE, pair.parentPKValue());
		}
	}

	// 删除与links有关的所有电子眼以及组成信息表
	public String updateRelectroniceye(Result result, List<Integer> linkPids) throws Exception {
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.conn);
		for (Integer linkPid : linkPids) {
			List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(linkPid, true);
			for (RdElectroniceye eleceye : eleceyes) {
				eleceye.changedFields().put("linkPid", 0);
				result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.pid());
			}
		}
		return null;
	}
	
	/**
	 * 删除link对电子眼的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getUpdateRdEyeInfectData(int linkPid,Connection conn) throws Exception {
		
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);

		List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(linkPid, true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdElectroniceye electr : eleceyes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(electr.objType());

			alertObj.setPid(electr.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
