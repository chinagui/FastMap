/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * OpRefDirectoroute
* @ClassName: OpRefDirectoroute 
* @author Zhang Xiaolong
* @date 2016年9月21日 下午4:10:43 
* @Description: TODO
 */
public class OpRefDirectoroute{
	
	private Connection conn;

	public OpRefDirectoroute(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,int crossPid) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation routeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(
				conn);

		routeOperation.deleteByCross(crossPid, result);
		
		return null;
	}

}
