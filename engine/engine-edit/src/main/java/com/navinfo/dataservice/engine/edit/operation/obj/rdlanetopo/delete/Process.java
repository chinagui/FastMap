package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneTopoDetailSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	private Check check = new Check();
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void postCheck() throws Exception {
		check.postCheck(this.getConn(), this.getResult(), this.getCommand().getDbId());
		super.postCheck();
	}
	@Override
	public boolean prepareData() throws Exception {
		RdLaneTopoDetailSelector selector =new RdLaneTopoDetailSelector(this.getConn());
		RdLaneTopoDetail detail = (RdLaneTopoDetail)selector.loadById(this.getCommand().getPid(), true);
		this.getCommand().setDetail(detail);
		
		return true;
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}


}
