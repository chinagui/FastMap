/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @ClassName: OpRefRdLane
 * @author 赵凯凯
 * @date 2016年8月18日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdLane {

	private Connection conn;

	public OpRefRdLane(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete.Operation(
				conn);
		operation.deleteRdLaneforRdLinks(command.getLinkPids(), result);
		return null;
	}

}
