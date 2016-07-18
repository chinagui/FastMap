package com.navinfo.dataservice.engine.edit.operation.obj.luface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;

public class Operation implements IOperation {
	private Command command;

	private Check check;

	private Connection conn;

	public Operation(Command command, Check check, Connection conn) {
		this.command = command;

		this.check = check;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		LuFace luFace = (LuFace) new LuFaceSelector(conn).loadById(
				command.getFaceId(), true);
		result.insertObject(luFace, ObjStatus.DELETE, luFace.getPid());
		return null;
	}

}
