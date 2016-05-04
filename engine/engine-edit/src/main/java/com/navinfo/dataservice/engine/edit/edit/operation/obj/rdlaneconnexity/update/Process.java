package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlaneconnexity.update;

import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdLaneConnexity lane;

	@Override
	public boolean prepareData() throws Exception {

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(this.getConn());

		this.lane = (RdLaneConnexity) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.lane, this.getConn()).run(this.getResult());
	}

}
