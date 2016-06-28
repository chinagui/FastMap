package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.update;

import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {


	private RdBranch branch;

	public Process(AbstractCommand command) throws Exception {
		super(command);


	}



	@Override
	public boolean prepareData() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(this.getConn());

		this.branch = (RdBranch) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),this.branch,this.getConn()).run(this.getResult());
	}

}
