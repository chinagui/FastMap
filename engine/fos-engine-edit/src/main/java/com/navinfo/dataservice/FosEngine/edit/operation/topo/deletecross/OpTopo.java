package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletecross;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkFormSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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

		result.insertObject(cross, ObjStatus.DELETE);

		RdLinkFormSelector selector = new RdLinkFormSelector(conn);

		for (IRow row : cross.getLinks()) {
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

			if(targetRow != null){
				if (forms.size()==1) {
					result.insertObject(targetRow, ObjStatus.UPDATE);
				}
				else{
					result.insertObject(targetRow, ObjStatus.DELETE);
				}
			}

		}
		return msg;
	}

}
