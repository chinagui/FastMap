package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;

public class OpRefParent implements IOperation{
	private CommandForDelete command;

	private IxPoiParent ixPoiParent;
	
	private IxPoiChildren ixPoiChildren;

	public OpRefParent(CommandForDelete command, IxPoiParent ixPoiParent,IxPoiChildren ixPoiChildren) {
		this.command = command;

		this.ixPoiParent = ixPoiParent;
		
		this.ixPoiChildren = ixPoiChildren;
	}

	@Override
	public String run(Result result) throws Exception {
		
		if(ixPoiParent != null)
		{
			result.insertObject(ixPoiParent, ObjStatus.DELETE, command.getPid());
		}
		else if(ixPoiChildren != null)
		{
			result.insertObject(ixPoiChildren, ObjStatus.DELETE, command.getPid());
		}
		
		return null;
	}
}
