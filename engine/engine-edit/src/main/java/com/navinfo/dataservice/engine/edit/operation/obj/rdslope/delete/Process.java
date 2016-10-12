package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;
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
		RdSlope slope  = (RdSlope)new RdSlopeSelector(this.getConn()).loadById(this.getCommand().getPid(), true);
		this.getCommand().setSlope(slope);
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
