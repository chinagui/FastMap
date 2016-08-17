package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;

public class Check {

	private Command command;

	public Check(Command command) {
		this.command = command;
	}

	/**
	 * @param conn
	 * @throws Exception
	 */
	public void hasRdVariableSpeed(Connection conn) throws Exception {

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		int inLinkPid = this.command.getInLinkPid();

		int nodePid = this.command.getNodePid();

		int outLinkPid = this.command.getOutLinkPid();
		
		RdVariableSpeed variableSpeed = selector.loadByInLinkNodeOutLinkPid(inLinkPid, nodePid, outLinkPid, true);
		
		if(variableSpeed != null)
		{
			throw new Exception("禁止重复创建可变限速，该线点线已经存在可变限速");
		}
	}
}
