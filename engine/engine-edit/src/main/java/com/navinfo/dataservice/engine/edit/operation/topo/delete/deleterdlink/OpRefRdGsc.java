package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;

public class OpRefRdGsc implements IOperation {
	
	private Command command;

	public OpRefRdGsc(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdGsc rdGsc : command.getRdGscs()){
			
			result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());
		}
		
		return null;
	}
	
	/**
	 * 删除link对立交的删除影响
	 * @return
	 */
	public List<AlertObject> getDeleteRdGscInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdGsc rdGsc : command.getRdGscs()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGsc.objType());

			alertObj.setPid(rdGsc.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
