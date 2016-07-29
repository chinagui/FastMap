package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.delete;

import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public boolean prepareData() throws Exception {
		AbstractSelector abSelector = new AbstractSelector(RdGate.class,this.getConn());
		this.getCommand().setRdGate((RdGate) abSelector.loadById(this.getCommand().getPid(), true));
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
