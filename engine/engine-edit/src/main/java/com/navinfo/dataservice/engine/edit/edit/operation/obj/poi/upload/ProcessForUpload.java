package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class ProcessForUpload extends AbstractProcess<CommandForUpload>implements IProcess{

	public ProcessForUpload(AbstractCommand command) throws Exception{
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		return null;
	}
}
