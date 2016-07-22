package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create;

import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {
	
	private Command command;
	
	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		RdElectroniceye entryEleceye = command.getEntryEleceye();
		RdElectroniceye exitEleceye = command.getExitEleceye();
		
		// 添加区间测速电子眼配对信息
		RdEleceyePair pair = new RdEleceyePair();
		pair.setPid(PidService.getInstance().applyEleceyePairPid());
		pair.setRowId(UuidUtils.genUuid());
		pair.setMesh(entryEleceye.mesh());
		result.insertObject(pair, ObjStatus.INSERT, pair.getPid());
		
		// 添加区间测速电子眼的起始电子眼
		RdEleceyePart part = new RdEleceyePart();
		part.setEleceyePid(entryEleceye.pid());
		part.setGroupId(pair.pid());
		part.setMesh(entryEleceye.mesh());
		part.setRowId(UuidUtils.genUuid());
		result.insertObject(part, ObjStatus.INSERT, part.getEleceyePid());
		
		// 添加区间测速电子眼的结束电子眼
		part = new RdEleceyePart();
		part.setEleceyePid(exitEleceye.pid());
		part.setGroupId(pair.pid());
		part.setMesh(exitEleceye.mesh());
		part.setRowId(UuidUtils.genUuid());
		result.insertObject(part, ObjStatus.INSERT, part.getEleceyePid());
		
		return null;
	}

}
