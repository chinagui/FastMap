package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;

public class Operation implements IOperation {

	private Command command;

	private RdCross cross;

	public Operation(Command command, RdCross cross) {
		this.command = command;

		this.cross = cross;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(cross, ObjStatus.DELETE, cross.getPid());
		
		//维护交通信号等
		List<IRow> trafficsignals = this.command.getTrafficsignals();
		
		if(CollectionUtils.isNotEmpty(trafficsignals))
		{
			for(IRow trafficsignal : trafficsignals)
			{
				result.insertObject(trafficsignal, ObjStatus.DELETE, trafficsignal.parentPKValue());
			}
		}
				
		return null;
	}

}
