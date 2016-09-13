package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;

public class OpRefLaneConnexity implements IOperation {
	
	private Command command;

	public OpRefLaneConnexity(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdLaneConnexity lane : command.getLanes()){
			
			result.insertObject(lane, ObjStatus.DELETE, lane.pid());
		}
		
		return null;
	}
	
	/**
	 * 删除link对车信的更新影响分析
	 * @return
	 */
	public List<AlertObject> getUpdateResInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : command.getOutLinkUpdateRdLaneConnexitys()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除进入link对车信的删除影响分析
	 * @return
	 */
	public List<AlertObject> getDeleteInLinkRdLaneConnexityInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : command.getInLinkRdLaneConnexitys()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
	
	/**
	 * 删除退出link对车信的删除影响分析
	 * @return
	 */
	public List<AlertObject> getDeleteOutLinkRdLanConnexityInfectData() {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdLaneConnexity rdLaneConnexity : command.getOutLinkDeleteRdLaneConnexitys()) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdLaneConnexity.objType());

			alertObj.setPid(rdLaneConnexity.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
