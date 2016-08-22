package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;

public class Operation implements IOperation {

	private RdVariableSpeed variableSpeed;
	
	private Connection conn;

	public Operation(RdVariableSpeed variableSpeed) {

		this.variableSpeed = variableSpeed;

	}
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());
				
		return null;
	}
	
	/**
	 * @param linkPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLink(RdLink link, Result result) throws Exception {
		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);
		
		List<RdVariableSpeed> variableSpeedList = selector.loadRdVariableSpeedByLinkPid(link.getPid(), true);
		
		for(RdVariableSpeed rdVariableSpeed : variableSpeedList)
		{
			result.insertObject(rdVariableSpeed, ObjStatus.DELETE, rdVariableSpeed.getPid());
		}
		
		List<RdVariableSpeedVia> viaList = selector.loadRdVariableSpeedVia(link.getPid(), true);
		
		for(RdVariableSpeedVia via : viaList)
		{
			result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
		}
	}
}
