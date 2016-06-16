package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.delete;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command>{

	private IxPoi ixPoi;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		IxPoiSelector selector = new IxPoiSelector(this.getConn());

		this.ixPoi = (IxPoi) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}
	
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.ixPoi).run(this.getResult());
	}
}
