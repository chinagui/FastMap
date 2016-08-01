package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;

public class Check {
	public void checkHasTrafficSignal(RdCross cross) throws Exception
	{
		if(cross != null)
		{
			if(cross.getSignal() == 1)
			{
				throw new Exception("该路口已存在路口红绿灯");
			}
		}
	}

	/**
	 * @param crossNode
	 * @throws Exception 
	 */
	public void checkHasCross(IRow crossNode) throws Exception {
		if(crossNode == null)
		{
			throw new Exception("红绿灯不能创建在非路口上");
		}
	}
}
