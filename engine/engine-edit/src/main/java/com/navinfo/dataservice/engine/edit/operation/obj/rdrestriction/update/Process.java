package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update;

import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

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
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.getConn(),this.restrict).run(this.getResult());
	}

	

}
