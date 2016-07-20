package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create;

import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private static final int ENTRY_KIND = 21;

	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		Command command = this.getCommand();
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.getConn());

		RdElectroniceye eleceye = (RdElectroniceye) selector.loadById(command.getEleceyePid1(), false);
		if (ENTRY_KIND == eleceye.getKind()) {
			command.setEntryEleceye(eleceye);
			eleceye = (RdElectroniceye) selector.loadById(command.getEleceyePid2(), false);
			command.setExitEleceye(eleceye);
		} else {
			command.setExitEleceye(eleceye);
			eleceye = (RdElectroniceye) selector.loadById(command.getEleceyePid2(), false);
			command.setEntryEleceye(eleceye);
		}

		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
