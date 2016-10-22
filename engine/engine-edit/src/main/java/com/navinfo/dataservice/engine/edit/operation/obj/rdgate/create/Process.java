package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.create;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.selector.rd.gate.RdGateSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public boolean prepareData() throws Exception {
		RdGateSelector selector = new RdGateSelector(this.getConn());
		
		int nodePid = this.getCommand().getNodePid();
		
		List<RdGate> gateList = selector.loadByNodePid(nodePid, true);
		
		if(CollectionUtils.isNotEmpty(gateList))
		{
			throw new Exception("点位已存在大门，不允许重复创建");
		}
		
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
	}

}
