package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn;

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

		result.setPrimaryPid(roadPid);

		for (IRow row : this.command.getRoad().getLinks()) {

			RdRoadLink roadLink = (RdRoadLink) row;

			result.insertObject(roadLink, ObjStatus.DELETE, roadLink.getPid());
		}

		int seqNum = 1;

		for (int linkPid : this.command.getLinkPids()) {
			// 创建新RdRoadLink
			RdRoadLink roadLink = new RdRoadLink();

			roadLink.setLinkPid(linkPid);

			roadLink.setPid(this.command.getRoad().pid());

			roadLink.setSeqNum(seqNum++);

			result.insertObject(roadLink, ObjStatus.INSERT, roadLink.getPid());
		}

		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation rdObjectUpdateOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
				this.conn);

		rdObjectUpdateOperation.updateRdObjectForRdRoad(this.command.getRoad(),
				result);

		return null;
	}

	/**
	 * 打断link维护rdroad。
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public String breakRdLink(int linkPid, List<RdLink> newLinks, Result result)
			throws Exception {

		RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(
				this.conn);

		List<Integer> linkPids = new ArrayList<>();

		linkPids.add(linkPid);

		List<RdRoadLink> rdRoadLinks = rdRoadLinkSelector.loadByLinks(linkPids,
				true);

		if (rdRoadLinks.size() != 1) {

			return null;
		}

		RdRoadLink deleteroadLink = rdRoadLinks.get(0);

		AbstractSelector roadSelector = new AbstractSelector(RdRoad.class,
				this.conn);

		RdRoad road = (RdRoad) roadSelector.loadById(deleteroadLink.getPid(),
				true, true);

		// 更新序号大于被删除rdroad的rdroad序号
		for (IRow roadRow : road.getLinks()) {

			RdRoadLink roadLink = (RdRoadLink) roadRow;

			if (roadLink.getSeqNum() > deleteroadLink.getSeqNum()) {

				roadLink.changedFields().put("seqNum",
						roadLink.getSeqNum() + newLinks.size() - 1);

				result.insertObject(roadLink, ObjStatus.UPDATE,
						roadLink.getPid());
			}
		}

		int seqNum = deleteroadLink.getSeqNum();

		// 创建新rdroad
		for (RdLink newLink : newLinks) {

			RdRoadLink newRoadLink = new RdRoadLink();

			newRoadLink.setLinkPid(newLink.pid());

			newRoadLink.setSeqNum(seqNum++);

			newRoadLink.setPid(deleteroadLink.getPid());

			result.insertObject(newRoadLink, ObjStatus.INSERT,
					newRoadLink.getPid());
		}

		// 删除被打断link对应的rdroad
		result.insertObject(deleteroadLink, ObjStatus.DELETE,
				deleteroadLink.getPid());

		return null;
	}
}
