package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update;

import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private RdVariableSpeed variableSpeed;

	@Override
	public boolean prepareData() throws Exception {

		this.variableSpeed = (RdVariableSpeed) new AbstractSelector(RdVariableSpeed.class,getConn()).loadById(this.getCommand().getPid(), true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.variableSpeed, this.getConn()).run(this.getResult());
	}

}
