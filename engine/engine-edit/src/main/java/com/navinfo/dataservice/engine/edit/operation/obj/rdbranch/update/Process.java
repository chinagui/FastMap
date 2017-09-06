package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

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
	
	@Override
	public void postCheck() throws Exception {
		List<IRow> glmList = new ArrayList<IRow>();
		glmList.addAll(this.getResult().getAddObjects());
		glmList.addAll(this.getResult().getUpdateObjects());
		
		for (IRow irow : this.getResult().getDelObjects()) {
			if (irow instanceof RdBranchVia) {
				glmList.add(irow);
			}
		}
		this.checkCommand.setGlmList(glmList);
		this.checkEngine.postCheck();
	}

}
