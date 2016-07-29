package com.navinfo.dataservice.engine.edit.operation.obj.lcface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;

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
		LcFace lcFace = (LcFace) new LcFaceSelector(conn).loadById(command.getFaceId(), true);
		result.insertObject(lcFace, ObjStatus.DELETE, lcFace.getPid());
		return null;
	}

		

}
