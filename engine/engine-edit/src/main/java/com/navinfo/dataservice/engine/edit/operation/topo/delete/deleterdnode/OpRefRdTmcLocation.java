/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @ClassName: OpRefTrafficsignal
 * @author Zhang Xiaolong
 * @date 2016年7月21日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdTmcLocation {

	private Connection conn;

	public OpRefRdTmcLocation(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation(
				this.conn);

		rdinterOperation.deleteLinkUpdateTmc(result,command.getLinks(),command.getLinkPids());
		
		return null;
	}

}
