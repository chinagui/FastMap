package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminGroup;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminPart;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;

	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		if (command.getParentType().equals("adadmin")) {
			
			AdAdminGroup adAdminGroup = new AdAdminGroup();
			
			adAdminGroup.setPid(PidService.getInstance().applyAdAdminGroupPid());
			
			adAdminGroup.setRegionIdUp(command.getPid());
			
			result.insertObject(adAdminGroup, ObjStatus.INSERT, adAdminGroup.getPid());
		}
		List<Integer> adadminIds = command.getAdAdminIds();

		for (int i = 0; i < adadminIds.size(); i++) {

			AdAdminPart adAdminPart = new AdAdminPart();

			adAdminPart.setGroupId(command.getPid());

			adAdminPart.setRegionIdDown(adadminIds.get(i));

			result.insertObject(adAdminPart, ObjStatus.INSERT, adAdminPart.getGroupId());
		}
		return msg;
	}

}
