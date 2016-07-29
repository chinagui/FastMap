package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;

public class OpRefLcFace implements IOperation {

	private Command command;

	private Result result;

	public OpRefLcFace(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleLcFace(command.getFaces());

		return null;
	}

	// 处理面
	private void handleLcFace(List<LcFace> list) throws Exception {
		if (list != null && list.size() > 0) {
			for (LcFace lcFace : list) {
				result.insertObject(lcFace, ObjStatus.DELETE, lcFace.getPid());
			}
		}
	}

}
