package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdspeedlimit.delete;

import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdSpeedlimit speedlimit;

	@Override
	public boolean prepareData() throws Exception {

		RdSpeedlimitSelector selector = new RdSpeedlimitSelector(this.getConn());

		this.speedlimit = (RdSpeedlimit) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.speedlimit).run(this.getResult());
	}

}
