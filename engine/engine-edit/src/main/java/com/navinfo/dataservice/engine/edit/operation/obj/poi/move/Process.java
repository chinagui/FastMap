package com.navinfo.dataservice.engine.edit.operation.obj.poi.move;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command>   implements IProcess {
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
	@Override
	public boolean prepareData() throws Exception {
		IxPoiSelector poiSelector = new IxPoiSelector(this.getConn());
		
		this.getCommand().setIxPoi((IxPoi)poiSelector.loadById(this.getCommand().getPid(), true));
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
