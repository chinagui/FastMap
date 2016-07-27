package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

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
}
