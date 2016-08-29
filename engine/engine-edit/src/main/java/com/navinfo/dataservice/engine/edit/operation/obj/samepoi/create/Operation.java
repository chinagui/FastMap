package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoiPart;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:23:07
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		IxSamepoi samePoi = new IxSamepoi();
		samePoi.setPid(PidService.getInstance().applySamepoiPid());
		result.insertObject(samePoi, ObjStatus.INSERT, samePoi.pid());

		IxSamepoiPart samePoiPart = null;
		for (String id : command.getPoiIds()) {
			samePoiPart = new IxSamepoiPart();
			samePoiPart.setGroupId(samePoi.pid());
			samePoiPart.setPoiPid(Integer.valueOf(id));
			result.insertObject(samePoiPart, ObjStatus.INSERT, samePoi.pid());
		}
		return null;
	}

}
