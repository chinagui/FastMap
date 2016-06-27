package com.navinfo.dataservice.engine.edit.edit.operation.obj.zoneface.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;

public class Operation implements IOperation {
	/**
	 * ZONE 面操作类
	 * 
	 */
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
		this.handleFace(result);
		return null;
	}
	private void handleFace(Result result) throws Exception{
		ZoneFace zoneFace = (ZoneFace) new ZoneFaceSelector(conn).loadById(command.getFaceId(), true);
		result.insertObject(zoneFace, ObjStatus.DELETE, zoneFace.getPid());
	}
		

}
