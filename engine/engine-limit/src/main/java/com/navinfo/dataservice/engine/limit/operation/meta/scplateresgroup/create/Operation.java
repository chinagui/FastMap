package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create;

import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;

public class Operation implements IOperation {
	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		ScPlateresGroup group = new ScPlateresGroup();

		group.setAdAdmin(this.command.getAdAdmin());

		String groupId = PidApply.getInstance(conn).pidForInsertGroup(
				this.command.getInfoIntelId(), this.command.getAdAdmin(), this.command.getCondition());

		group.setGroupId(groupId);

		group.setGroupType(this.command.getGroupType());

		group.setPrinciple(this.command.getPrinciple());

		group.setInfoIntelId(this.command.getInfoIntelId());

		result.insertObject(group, ObjStatus.INSERT, group.getGroupId());

		return null;
	}
}
