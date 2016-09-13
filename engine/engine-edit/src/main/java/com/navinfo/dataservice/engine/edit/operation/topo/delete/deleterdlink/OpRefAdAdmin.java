package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;

public class OpRefAdAdmin implements IOperation {

	private Command command;

	public OpRefAdAdmin(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		for (AdAdmin adAdmin : command.getAdAdmins()) {

			adAdmin.changedFields().put("linkPid", 0);

			result.insertObject(adAdmin, ObjStatus.UPDATE, adAdmin.pid());
		}

		return null;
	}

	/**
	 * 删除link对行政区划代表点的更新影响分析
	 * @return
	 */
	public List<AlertObject> getUpdateAdminInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (AdAdmin adAdmin : command.getAdAdmins()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(adAdmin.objType());

			alertObj.setPid(adAdmin.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
