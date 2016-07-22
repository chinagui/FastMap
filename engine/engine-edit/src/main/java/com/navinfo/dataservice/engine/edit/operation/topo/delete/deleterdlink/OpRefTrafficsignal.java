/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;

/** 
* @ClassName: OpRefTrafficsignal 
* @author Zhang Xiaolong
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefTrafficsignal implements IOperation{
	
	private Command command;

	public OpRefTrafficsignal(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {
		
		RdTrafficsignal rdTrafficsignal = this.command.getTrafficSignal();
		if(rdTrafficsignal != null)
		{
			result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.getPid());
		}
		return null;
	}

}
