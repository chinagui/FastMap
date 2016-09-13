package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

public class OpRefCross implements IOperation {

	private Command command;

	private Connection conn;

	public OpRefCross(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		List<Integer> nodePids = command.getNodePids();

		for (RdCross cross : command.getCrosses()) {
			deleteCross(cross, result, nodePids);
		}
		return null;
	}

	private void deleteCross(RdCross cross, Result result, List<Integer> nodePids) {

		List<IRow> nodes = new ArrayList<IRow>();

		// 判断线上是否还存在其他路口：1.存在,将关系表数据删除2.不存在，直接讲路口删除
		for (IRow row : cross.getNodes()) {
			RdCrossNode node = (RdCrossNode) row;

			if (nodePids.contains(node.getNodePid())) {
				nodes.add(node);
			}
		}

		if (nodes.size() == cross.getNodes().size()) {
			result.insertObject(cross, ObjStatus.DELETE, cross.pid());
		} else {
			for (IRow node : nodes) {
				result.insertObject(node, ObjStatus.DELETE, cross.pid());
			}

			for (IRow row : cross.getLinks()) {
				RdCrossLink link = (RdCrossLink) row;

				if (link.getLinkPid() == command.getLinkPid()) {
					result.insertObject(link, ObjStatus.DELETE, cross.pid());
					break;
				}
			}
		}
	}

	public List<AlertObject> getDeleteRdCross() {
		List<AlertObject> alertList = new ArrayList<>();

		List<Integer> nodePids = command.getNodePids();

		for (RdCross cross : command.getCrosses()) {
			List<IRow> nodes = new ArrayList<IRow>();

			AlertObject alertObj = new AlertObject();

			// 判断线上是否还存在其他路口：1.存在,将关系表数据删除2.不存在，直接讲路口删除
			for (IRow row : cross.getNodes()) {
				RdCrossNode node = (RdCrossNode) row;

				if (nodePids.contains(node.getNodePid())) {
					nodes.add(node);
				}
			}

			if (nodes.size() == cross.getNodes().size()) {
				alertObj.setObjType(cross.objType());

				alertObj.setPid(cross.getPid());

				alertObj.setStatus(ObjStatus.DELETE);
			}
		}

		return alertList;
	}

	public List<AlertObject> getUpdateRdCross() {
		List<AlertObject> alertList = new ArrayList<>();

		List<Integer> nodePids = command.getNodePids();

		for (RdCross cross : command.getCrosses()) {
			List<IRow> nodes = new ArrayList<IRow>();

			// 判断线上是否还存在其他路口：1.存在,将关系表数据删除2.不存在，直接讲路口删除
			for (IRow row : cross.getNodes()) {
				RdCrossNode node = (RdCrossNode) row;

				if (nodePids.contains(node.getNodePid())) {
					nodes.add(node);
				}
			}

			if (nodes.size() != cross.getNodes().size()) {
				AlertObject alertObj = new AlertObject();

				alertObj.setObjType(cross.objType());

				alertObj.setPid(cross.getPid());

				alertObj.setStatus(ObjStatus.UPDATE);
				
				alertList.add(alertObj);

//				for (IRow row : cross.getLinks()) {
//					RdCrossLink link = (RdCrossLink) row;
//
//					if (link.getLinkPid() == command.getLinkPid()) {
//						AlertObject alertObj = new AlertObject();
//
//						alertObj.setObjType(cross.objType());
//
//						alertObj.setPid(cross.getPid());
//
//						alertObj.setStatus(ObjStatus.DELETE);
//						break;
//					}
//				}
			}
		}

		return alertList;
	}
}
