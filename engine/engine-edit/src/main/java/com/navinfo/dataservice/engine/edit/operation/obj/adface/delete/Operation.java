package com.navinfo.dataservice.engine.edit.operation.obj.adface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;

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
		AdFace adFace = (AdFace) new AdFaceSelector(conn).loadById(command.getFaceId(), true);
		result.insertObject(adFace, ObjStatus.DELETE, adFace.getPid());
		return null;
	}

		

}
