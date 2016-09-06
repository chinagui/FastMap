package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;


public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		createVariableSpeed(result);
		return null;
	}

	/**
	 * @param result
	 * @throws Exception 
	 */
	private void createVariableSpeed(Result result) throws Exception {
		
		int inLinkPid = this.command.getInLinkPid();
		
		int nodePid = this.command.getNodePid();
		
		int outLinkPid = this.command.getOutLinkPid();
		
		RdVariableSpeed variableSpeed = new RdVariableSpeed();
		
		variableSpeed.setPid(PidUtil.getInstance().applyRdVariableSpeedPid());
		
		variableSpeed.setInLinkPid(inLinkPid);
		
		variableSpeed.setNodePid(nodePid);

		variableSpeed.setOutLinkPid(outLinkPid);
		
		List<Integer> viaList = this.command.getVias();
		
		if(CollectionUtils.isNotEmpty(viaList))
		{
			for(int i = 0;i<viaList.size();i++)
			{
				RdVariableSpeedVia variableSpeedVia = new RdVariableSpeedVia();
				
				variableSpeedVia.setLinkPid(viaList.get(i));
				
				variableSpeedVia.setVspeedPid(variableSpeed.getPid());
				
				variableSpeedVia.setSeqNum(i+1);
				
				variableSpeed.getVias().add(variableSpeedVia);
			}
		}
		
		result.insertObject(variableSpeed, ObjStatus.INSERT, variableSpeed.getPid());
	}
}
