package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		delRelectroniceye(result);
		return null;
	}

	public void delRelectroniceye(Result result) {
		// 删除电子眼
		result.insertObject(this.command.getEleceye(), ObjStatus.DELETE, this.command.getEleceye().parentPKValue());
		// 删除电子眼组成关系表(同时删除子表信息)
		for (IRow pair : this.command.getEleceye().getPairs()) {
			result.insertObject(pair, ObjStatus.DELETE, pair.parentPKValue());
		}
	}

	// 删除与links有关的所有电子眼以及组成信息表
	public String deleteRelectroniceye(Result result, List<Integer> linkPids) throws Exception {
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.conn);
		for (Integer linkPid : linkPids) {
			List<RdElectroniceye> eleceyes = selector.loadListByRdLinkId(linkPid, true);
			for (RdElectroniceye eleceye : eleceyes) {
				result.insertObject(eleceye, ObjStatus.DELETE, eleceye.pid());
				for (IRow pair : eleceye.getPairs()) {
					result.insertObject(pair, ObjStatus.DELETE, pair.parentPKValue());
				}
			}
		}
		return null;
	}

}
