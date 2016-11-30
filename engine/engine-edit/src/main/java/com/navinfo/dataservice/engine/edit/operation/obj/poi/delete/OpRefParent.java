package com.navinfo.dataservice.engine.edit.operation.obj.poi.delete;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;

public class OpRefParent implements IOperation{
	private Command command;

	private List<IRow> ixPoiParents;
	
	private IxPoiChildren ixPoiChildren;

	public OpRefParent(Command command, List<IRow> ixPoiParents,IxPoiChildren ixPoiChildren) {
		this.command = command;

		this.ixPoiParents = ixPoiParents;
		
		this.ixPoiChildren = ixPoiChildren;
	}

	@Override
	public String run(Result result) throws Exception {
		
		if(CollectionUtils.isNotEmpty(ixPoiParents))
		{
			for(IRow parent : ixPoiParents)
			{
				result.insertObject(parent, ObjStatus.DELETE, command.getPid());
			}
		}
		if(ixPoiChildren != null)
		{
			result.insertObject(ixPoiChildren, ObjStatus.DELETE, command.getPid());
		}
		
		return null;
	}
}
