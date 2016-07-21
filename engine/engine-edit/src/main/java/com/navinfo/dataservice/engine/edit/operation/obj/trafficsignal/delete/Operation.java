package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:02 
* @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(command.getRdTrafficsignal(), ObjStatus.DELETE, command.getPid());
		
		//维护路口signal属性(如果此路口的“信号灯”原为“有路口红绿灯”，则将其修改为“无红绿灯”)
		RdCross cross = this.command.getRdCross();
		
		if(cross != null)
		{
			cross.changedFields().put("signal", 0);
		}
		
		result.insertObject(cross, ObjStatus.UPDATE, cross.getPid());
				
		return null;
	}

}
