/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
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
	
	public String run(Result result,List<IRow> allNodes) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
				conn);

		trafficSignalOperation.deleteByNode(result,allNodes);
		
		return null;
	}

}
