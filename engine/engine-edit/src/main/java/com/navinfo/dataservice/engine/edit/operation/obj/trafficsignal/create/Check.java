package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

public class Check {
	
	private Connection conn;
	
	public Check(Connection conn)
	{
		this.conn = conn;
	}
	
	/**
	 * 检查是否存在路口红绿灯
	 * @param cross
	 * @param nodePid
	 * @throws Exception
	 */
	public void checkHasTrafficSignal(RdCross cross,int nodePid) throws Exception
	{
		if(cross != null)
		{
			AbstractSelector abstractSelector = new AbstractSelector(conn);
			
			List<IRow> rows = abstractSelector.loadRowsByClassParentId(RdCrossNode.class, cross.getPid(), true, null, null);
			
			List<Integer> crossNodePidList = new ArrayList<>();
			
			for(IRow row : rows)
			{
				RdCrossNode crossNode = (RdCrossNode) row;
				
				crossNodePidList.add(crossNode.getNodePid());
			}
			
			RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);
			
			List<RdTrafficsignal> trafficsignals = selector.loadByNodePids(crossNodePidList, true);
			
			if(cross.getSignal() == 1)
			{
				if(CollectionUtils.isNotEmpty(trafficsignals))
				{
					throw new Exception("该路口已存在路口红绿灯");
				}
			}
			else
			{
				if(CollectionUtils.isNotEmpty(trafficsignals))
				{
					throw new Exception("路口信号灯属性有误：属性为非“有路口红绿灯”");
				}
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
