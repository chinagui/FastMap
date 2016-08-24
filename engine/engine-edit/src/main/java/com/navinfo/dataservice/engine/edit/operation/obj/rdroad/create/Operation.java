package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = create(result);

		return msg;
	}

	private String create(Result result) throws Exception {

		RdRoad road = new RdRoad();

		road.setPid(PidService.getInstance().applyRdVoiceguidePid());

		List<IRow> roadLinks = new ArrayList<IRow>();

		for (int i = 0; i < this.command.getLinkPids().size(); i++) {

			RdRoadLink roadLink = new RdRoadLink();

			roadLink.setLinkPid(this.command.getLinkPids().get(i));

			roadLink.setPid(road.pid());

			roadLink.setSeqNum(i + 1);

			roadLinks.add(roadLink);
		}

		road.setLinks(roadLinks);

		result.insertObject(road, ObjStatus.INSERT, road.pid());

		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectUpdateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
				this.conn);

		rdObjectUpdateOperation.updateRdObjectForRdRoad(road, result);

		return null;
	}

}
