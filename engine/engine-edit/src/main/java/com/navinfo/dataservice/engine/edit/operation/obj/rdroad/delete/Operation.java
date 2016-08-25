package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

public class Operation implements IOperation {

	private Command command = null;

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

		msg = delete(result, command.getRoad());

		return msg;
	}

	private String delete(Result result, RdRoad road) {

		result.insertObject(road, ObjStatus.DELETE, road.pid());

		return null;
	}

	public String deleteByLinks(List<Integer> linkPids, Result result)
			throws Exception {

		RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(
				this.conn);

		List<RdRoadLink> rdRoadLinks = rdRoadLinkSelector.loadByLinks(linkPids,
				true);
		
		if(rdRoadLinks.size()<1)
		{
			return null;
		}

		List<Integer> rdRoadPids = new ArrayList<Integer>();

		for (RdRoadLink roadLink : rdRoadLinks) {

			if (!rdRoadPids.contains(roadLink.getPid())) {

				rdRoadPids.add(roadLink.getPid());
			}
		}

		AbstractSelector roadSelector = new AbstractSelector(RdRoad.class,
				this.conn);

		List<IRow> rows = roadSelector.loadByIds(rdRoadPids, true, true);

		Set<Integer> deleteLinkbyRoad = new HashSet<Integer>();

		for (IRow roadRow : rows) {

			RdRoad road = (RdRoad) roadRow;

			boolean isAllDelete = true;

			for (IRow linkRow : road.getLinks()) {

				RdRoadLink roadLink = (RdRoadLink) linkRow;

				if (!linkPids.contains(roadLink.getLinkPid())) {

					isAllDelete = false;

					continue;
				}

				deleteLinkbyRoad.add(roadLink.getLinkPid());
			}

			if (isAllDelete) {

				delete(result, road);
			}
		}

		for (RdRoadLink roadLink : rdRoadLinks) {

			if (!deleteLinkbyRoad.contains(roadLink.getLinkPid())) {

				result.insertObject(roadLink, ObjStatus.DELETE,
						roadLink.getPid());
			}
		}

		return null;

	}
}
