package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create;

import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	private Check check;

	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String preCheck() throws Exception {
		check.checkRdEleceyePair(this.getCommand());
		return null;
	}

	@Override
	public boolean prepareData() throws Exception {
		// 根据pid1和pid2加载配对电子眼
		Command command = this.getCommand();
		RdElectroniceyeSelector selector = new RdElectroniceyeSelector(this.getConn());

		RdElectroniceye eleceye = (RdElectroniceye) selector.loadById(command.getEleceyePid1(), false);
		if (Command.ENTRY_KIND == eleceye.getKind()) {
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
