package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.selector.rd.lane.RdLaneSelector;
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
		RdLaneSelector selector =new RdLaneSelector(this.getConn());
		RdLane lane  = (RdLane)selector.loadById(this.getCommand().getPid(), true);
		this.getCommand().setRdLane(lane);
		this.getCommand().setLanes(selector.loadByLink(lane.getLinkPid(), lane.getLaneDir(), true));
		
		return true;
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}


}
