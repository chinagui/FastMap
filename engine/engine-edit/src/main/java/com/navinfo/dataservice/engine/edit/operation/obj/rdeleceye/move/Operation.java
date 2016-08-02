package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;

import net.sf.json.JSONObject;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月29日 下午3:48:28
 * @version: v1.0
 */
public class Operation implements IOperation {

	public Operation() {
	}

	public Operation(Command command) {
		this.command = command;
	}

	private Command command;

	@Override
	public String run(Result result) throws Exception {
		RdElectroniceye electroniceye = this.command.getEleceye();
		boolean isChanged = electroniceye.fillChangeFields(this.command.getContent());
		if (isChanged) {
			result.insertObject(electroniceye, ObjStatus.UPDATE, electroniceye.pid());
			result.setPrimaryPid(electroniceye.pid());
		}
		return null;
	}

}
