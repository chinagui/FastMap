package com.navinfo.dataservice.engine.edit.operation.obj.poi.upload.create;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperStage;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command>implements IProcess{

	public Process(AbstractCommand command) throws Exception{
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		IxPoi poi = new IxPoi();
		poi.Unserialize(this.getCommand().getPoi());
		Result result = new Result();
		result.setOperStage(OperStage.Collect);
		result.insertObject(poi, ObjStatus.INSERT, poi.getPid());
		this.setResult(result);
		return null;
	}
}
