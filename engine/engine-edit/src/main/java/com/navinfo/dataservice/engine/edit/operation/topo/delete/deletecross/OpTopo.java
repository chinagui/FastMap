package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkFormSelector;

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
		
		//删除信号灯
		OpRefTrafficsignal opRefTrafficsignal = new OpRefTrafficsignal(conn);
		
		opRefTrafficsignal.run(result, cross.getNodes());

		return msg;
	}

	private void updateLinkForm(Result result) throws Exception {
		RdLinkFormSelector selector = new RdLinkFormSelector(conn);

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

}
