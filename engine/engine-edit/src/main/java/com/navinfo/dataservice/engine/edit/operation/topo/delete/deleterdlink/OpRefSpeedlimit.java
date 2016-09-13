package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class OpRefSpeedlimit implements IOperation {
	
	private Command command;

	public OpRefSpeedlimit(Command command) {
		this.command = command;
	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdSpeedlimit limit : command.getLimits()){
			
			result.insertObject(limit, ObjStatus.DELETE, limit.pid());
		}
		
		return null;
	}
	
	/**
	 * 删除link对限速的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteSpeedLimitInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdSpeedlimit rdSpeedlimit : command.getLimits()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdSpeedlimit.objType());

			alertObj.setPid(rdSpeedlimit.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
