package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

public class Check {
	
	private Connection conn;
	
	public Check(Connection conn)
	{
		this.conn = conn;
	}
	
	public void checkHasTrafficSignal(RdCross cross,int nodePid) throws Exception
	{
		if(cross != null)
		{
			if(cross.getSignal() == 1)
			{
				StringBuilder sb = new StringBuilder();
				
				RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);
				
				List<RdTrafficsignal> trafficsignals = selector.loadByNodeId(true, nodePid);
				
				for(RdTrafficsignal rdTrafficsignal : trafficsignals)
				{
					sb.append(rdTrafficsignal.getPid()+",");
				}
				
				throw new Exception("该路口已存在路口红绿灯,pid="+sb.deleteCharAt(sb.lastIndexOf(",")));
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
