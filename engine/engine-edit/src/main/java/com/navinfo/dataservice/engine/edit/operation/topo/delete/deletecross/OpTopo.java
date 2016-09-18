package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

public class OpTopo implements IOperation {

	private Command command;

	private Connection conn;

	public OpTopo(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		RdCross cross = command.getCross();

		result.insertObject(cross, ObjStatus.DELETE, cross.pid());

		result.setPrimaryPid(cross.getPid());

		updateLinkForm(result);

		return msg;
	}

	private void updateLinkForm(Result result) throws Exception {
		ISelector selector = new AbstractSelector(RdLinkForm.class, conn);

		for (IRow row : command.getCross().getLinks()) {
			RdCrossLink crosslink = (RdCrossLink) row;

			int linkPid = crosslink.getLinkPid();

			List<IRow> forms = selector.loadRowsByParentId(linkPid, true);

			IRow targetRow = null;

			for (IRow formrow : forms) {

				RdLinkForm form = (RdLinkForm) formrow;

				if (form.getFormOfWay() == 50) { // 交叉点内道路
					form.changedFields().put("formOfWay", 1);

					targetRow = form;
				}
			}

			if (targetRow != null) {
				if (forms.size() == 1) {
					result.insertObject(targetRow, ObjStatus.UPDATE, command.getCross().pid());
				} else {
					result.insertObject(targetRow, ObjStatus.DELETE, command.getCross().pid());
				}
			}

		}
	}
	
	public List<AlertObject> getDeleteRdCross(int linkPid, Connection conn) throws Exception {

		RdNodeSelector selector = new RdNodeSelector(conn);

		List<RdNode> nodelist = selector.loadEndRdNodeByLinkPid(linkPid, false);

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdNode node : nodelist) {
			nodePids.add(node.getPid());
		}

		RdCrossSelector crossSelector = new RdCrossSelector(conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(linkPid);

		List<RdCross> crossList = crossSelector.loadRdCrossByNodeOrLink(nodePids, linkPids, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdCross cross : crossList) {
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

	public List<AlertObject> getUpdateRdCross(int linkPid, Connection conn) throws Exception {
		RdNodeSelector selector = new RdNodeSelector(conn);

		List<RdNode> nodelist = selector.loadEndRdNodeByLinkPid(linkPid, false);

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdNode node : nodelist) {
			nodePids.add(node.getPid());
		}

		RdCrossSelector crossSelector = new RdCrossSelector(conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		linkPids.add(linkPid);

		List<RdCross> crossList = crossSelector.loadRdCrossByNodeOrLink(nodePids, linkPids, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdCross cross : crossList) {
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
			}
		}

		return alertList;
	}
}
