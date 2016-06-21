package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.update;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

	private IxPoi ixPoi;

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {

		IxPoiSelector selector = new IxPoiSelector(this.getConn());

		this.ixPoi = (IxPoi) selector
				.loadById(this.getCommand().getPid(), true);

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.ixPoi,this.getConn()).run(this
				.getResult());

	}

}
