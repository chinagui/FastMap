package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdRestriction restrict;

	@Override
	public boolean prepareData() throws Exception {

		RdRestrictionSelector selector = new RdRestrictionSelector(this.getConn());

		this.restrict = (RdRestriction) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.restrict);
	}
	

}
