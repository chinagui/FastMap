package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class ProcessForUpload extends AbstractProcess<CommandForUpload>{

	private IxPoi ixPoi;

	public ProcessForUpload(AbstractCommand command) throws Exception {
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
		return null;
	}
}
