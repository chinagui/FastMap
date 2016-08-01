package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {
	
	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		Map<Integer,List<Integer>> nodeLinkPidMap = this.command.getNodeLinkPidMap();
		
		createRdTrafficSignal(result,nodeLinkPidMap);
		
		return msg;
	}

	/**
	 * @param nodeLinkPidMap
	 * @throws Exception 
	 */
	private void createRdTrafficSignal(Result result,Map<Integer, List<Integer>> nodeLinkPidMap) throws Exception {
		if(nodeLinkPidMap.size()>0)
		{
			//复合路口和简单路口通用写法
			for(Map.Entry<Integer, List<Integer>> entry: nodeLinkPidMap.entrySet())
			{
				int nodePid = entry.getKey();
				
				List<Integer> linkPidList = entry.getValue();
				
				for(int linkPid : linkPidList)
				{
					RdTrafficsignal rdTrafficsignal = createRdTrafficSignal(nodePid,linkPid);
					
					result.insertObject(rdTrafficsignal, ObjStatus.INSERT, rdTrafficsignal.pid());
				}
			}
			//维护路口关系
			RdCross cross = this.command.getCross();
			
			cross.changedFields().put("signal", 1);
			
			result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
			
		}
		else
		{
			throw new Exception("该路口没有进入线可以创建信号灯");
		}
		
	}

	/**
	 * @param linkPid 进入线Pid
	 * @throws Exception 
	 */
	private RdTrafficsignal createRdTrafficSignal(int nodePid,int linkPid) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
		
		rdTrafficsignal.setPid(PidService.getInstance().applyRdTrafficsignalPid());
		
		rdTrafficsignal.setLinkPid(linkPid);
		
		rdTrafficsignal.setNodePid(nodePid);
		
		//默认为受控制
		rdTrafficsignal.setFlag(1);
		
		return rdTrafficsignal;
		
	}

}
