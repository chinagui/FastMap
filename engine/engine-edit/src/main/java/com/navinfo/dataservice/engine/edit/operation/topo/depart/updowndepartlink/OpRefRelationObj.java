package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;

public class OpRefRelationObj {

	private Connection conn = null;

	public OpRefRelationObj(Connection conn) {

		this.conn = conn;
	}

	// 同一线
	public String handleSameLink(Command command, Result result)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete.Operation(
				conn);

		operation.deleteByUpDownPartLink(command.getLinks(), result);

		return null;
	}

	// 警示信息
	public String handlerdWarninginfo(Command command, Result result)
			throws Exception {

		// 维护警示信息
		com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.delete.Operation(
				conn);

		warninginfoOperation.batchDeleteByLink(command.getLinks(), result);

		return null;
	}

	// 点限速
	public String handlerdSpeedlimit(Command command, Result result)
			throws Exception {

		// 维护警示信息
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation warninginfoOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Operation(
				conn);

		warninginfoOperation.upDownLink(command.getsNode(), command.getLinks(),
				command.getLeftLinkMapping(), command.getRightLinkMapping(),
				result);

		return null;
	}

}