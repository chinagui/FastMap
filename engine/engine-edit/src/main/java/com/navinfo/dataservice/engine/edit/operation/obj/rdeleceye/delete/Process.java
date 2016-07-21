package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	
	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}


	@Override
	public boolean prepareData() throws Exception {
		// 根据EleceyePid加载需要删除的RdElectroniceye
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.getConn());
		this.getCommand().setEleceye((RdElectroniceye) selector.loadById(this.getCommand().getPid(), false));
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
