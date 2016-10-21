package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete;

import java.sql.Connection;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

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
		
		if (CollectionUtils.isNotEmpty(result.getAddObjects()) || CollectionUtils.isNotEmpty(result.getUpdateObjects())
				|| CollectionUtils.isNotEmpty(result.getDelObjects())) {
			// 修改poi主表时间
			IxPoiSelector selector = new IxPoiSelector(conn);
			
			IxPoi ixPoi = (IxPoi) selector.loadById(this.command.getObjId(), true,true);
			
			ixPoi.changedFields().put("uDate", StringUtils.getCurrentTime());

			result.insertObject(ixPoi, ObjStatus.UPDATE, ixPoi.pid());
		}
		
		return null;
	}
	
}
