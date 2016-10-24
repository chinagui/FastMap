package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;

public class Operation implements IOperation {

	private Command command;

	private IxPoiParent ixPoiParent;
	
	private IxPoiChildren ixPoiChildren;
	
	private Connection conn;

	public Operation(Command command, IxPoiParent ixPoiParent,IxPoiChildren ixPoiChildren,Connection conn) {
		this.command = command;

		this.ixPoiParent = ixPoiParent;
		
		this.ixPoiChildren = ixPoiChildren;
		
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		
		if(ixPoiParent != null)
		{
			result.insertObject(ixPoiParent, ObjStatus.DELETE, command.getObjId());
		}
		else if(ixPoiChildren != null)
		{
			this.command.setObjType(ObjType.IXPOICHILDREN);
			result.insertObject(ixPoiChildren, ObjStatus.DELETE, command.getObjId());
		}
		
		return null;
	}
	
}
