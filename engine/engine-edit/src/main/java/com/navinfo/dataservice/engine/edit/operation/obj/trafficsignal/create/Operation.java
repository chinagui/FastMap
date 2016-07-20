package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

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

		Set<Integer> linkPidSet = this.command.getLinkPidSet();
		
		if(CollectionUtils.isNotEmpty(linkPidSet))
		{
			for(Integer pid : linkPidSet)
			{
				RdTrafficsignal rdTrafficsignal = createRdTrafficSignal(pid);
				
				result.insertObject(rdTrafficsignal, ObjStatus.INSERT, rdTrafficsignal.pid());
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
		
		return msg;
	}

	/**
	 * @param linkPid 进入线Pid
	 * @throws Exception 
	 */
	private RdTrafficsignal createRdTrafficSignal(int linkPid) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
		
		rdTrafficsignal.setPid(PidService.getInstance().applyRdTrafficsignalPid());
		
		rdTrafficsignal.setLinkPid(linkPid);
		
		rdTrafficsignal.setNodePid(this.command.getNodePid());
		
		//默认为受控制
		rdTrafficsignal.setFlag(1);
		
		return rdTrafficsignal;
		
	}

}
