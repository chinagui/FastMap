package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn;

	public Operation(Command command) {

		this.command = command;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = update(result);

		return null;
	}

	/**
	 * 更新
	 * 
	 * @param result
	 * @return
	 */
	private String update(Result result) throws Exception {

		int roadPid = this.command.getRoad().pid();

		for (IRow row : this.command.getRoad().getLinks()) {

			RdRoadLink roadLink = (RdRoadLink) row;

			result.insertObject(roadLink, ObjStatus.DELETE, roadPid);
		}

		int seqNum = 1;

		for (int linkPid : this.command.getLinkPids()) {
			// 创建新RdRoadLink
			RdRoadLink roadLink = new RdRoadLink();

			roadLink.setLinkPid(linkPid);

			roadLink.setPid(this.command.getRoad().pid());

			roadLink.setSeqNum(seqNum++);

			result.insertObject(roadLink, ObjStatus.INSERT, roadPid);
		}

		return null;
	}
}
