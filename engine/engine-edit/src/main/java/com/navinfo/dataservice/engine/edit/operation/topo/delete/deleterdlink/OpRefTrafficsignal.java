/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/** 
* @ClassName: OpRefTrafficsignal 
* @author Zhang Xiaolong
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefTrafficsignal{
	
	private Connection conn;

	public OpRefTrafficsignal(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,int linkPid) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
				conn);

		trafficSignalOperation.deleteByLink(result, linkPid);
		
		return null;
	}

}
