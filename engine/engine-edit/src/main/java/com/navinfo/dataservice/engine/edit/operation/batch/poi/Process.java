package com.navinfo.dataservice.engine.edit.operation.batch.poi;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command> implements IProcess {

	private IxPoi ixPoi;
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean prepareData() throws Exception {

		IxPoiSelector selector = new IxPoiSelector(this.getConn());

		this.ixPoi = (IxPoi) selector
				.loadById(this.getCommand().getPid(), this.getCommand().isLock());

		return true;
	}


	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.ixPoi).run(this.getResult());
	}

}
