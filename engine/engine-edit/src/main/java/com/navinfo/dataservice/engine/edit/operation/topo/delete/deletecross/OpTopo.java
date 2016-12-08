package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
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

	public OpTopo() {
	}

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

	/**
	 * 删除路口维护路口内link的形态
	 * @param result
	 * @throws Exception
	 */
	private void updateLinkForm(Result result) throws Exception {
		ISelector selector = new AbstractSelector(RdLinkForm.class, conn);

		for (IRow row : command.getCross().getLinks()) {
			RdCrossLink crosslink = (RdCrossLink) row;

			int linkPid = crosslink.getLinkPid();

			List<IRow> forms = selector.loadRowsByParentId(linkPid, true);

			// 需要删除的linkForm
			List<RdLinkForm> deleteFormList = new ArrayList<>();

			for (IRow formrow : forms) {
				RdLinkForm form = (RdLinkForm) formrow;

				if (form.getFormOfWay() == 50 || form.getFormOfWay() == 35) { // 交叉点内道路
					deleteFormList.add(form);
				}
			}

			//需要删除所有的交叉口和掉头口形态后，link没有其他形态，需要将link形态维护为无属性
			if(deleteFormList.size() == forms.size())
			{
				RdLinkForm updateForm = deleteFormList.remove(0);
				
				if(updateForm.getFormOfWay() != 1)
				{
					updateForm.changedFields().put("formOfWay", 1);
					
					result.insertObject(updateForm, ObjStatus.UPDATE, command.getCross().pid());
				}
			}
			
			for(RdLinkForm linkForm : deleteFormList)
			{
				result.insertObject(linkForm, ObjStatus.DELETE, command.getCross().pid());
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

				if (!alertList.contains(alertObj)) {
					alertList.add(alertObj);
				}
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

				if (!alertList.contains(alertObj)) {
					alertList.add(alertObj);
				}
			}
		}

		return alertList;
	}

	/**
	 * 删除路口的提示
	 * 
	 * @param crossPid
	 *            路口pid
	 * @param conn
	 *            数据库conn
	 * @return 提示信息
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossInfectData(int crossPid) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		AlertObject alertObj = new AlertObject();

		alertObj.setObjType(ObjType.RDCROSS);

		alertObj.setPid(crossPid);

		alertObj.setStatus(ObjStatus.DELETE);

		if (!alertList.contains(alertObj)) {
			alertList.add(alertObj);
		}

		return alertList;
	}
}
