package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			RdElectroniceye eleceye = command.getEleceye();

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				// 删除电子眼
				result.insertObject(eleceye, ObjStatus.DELETE, eleceye.parentPKValue());

				return null;
			} else {
				// 修改电子眼
				boolean isChanged = eleceye.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(eleceye, ObjStatus.UPDATE, eleceye.parentPKValue());

				}
			}
		}

		return null;
	}

}
