package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;

public class OpRefElectroniceye implements IOperation {

	private Command command;

	public OpRefElectroniceye(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		for (RdElectroniceye eleceye : command.getElectroniceyes()) {
			// 删除电子眼、区间测速电子眼
			result.insertObject(eleceye, ObjStatus.DELETE, eleceye.pid());

			for (IRow pair : eleceye.getPairs()) {
				// 删除电子眼组成信息表
				result.insertObject((RdEleceyePair) pair, ObjStatus.DELETE, pair.parentPKValue());
			}
		}

		return null;
	}
	
	/**
	 * 删除link对电子眼的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteElectroniceyeInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdElectroniceye eye : command.getElectroniceyes()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(eye.objType());

			alertObj.setPid(eye.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
