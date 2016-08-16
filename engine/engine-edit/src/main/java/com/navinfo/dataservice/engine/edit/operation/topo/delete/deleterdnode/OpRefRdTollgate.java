/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * 
 * @Title: OpRefRdTollgate.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月11日 上午11:34:52
 * @version: v1.0
 */
public class OpRefRdTollgate {

	private Connection conn;

	private Command command;

	public OpRefRdTollgate(Connection conn, Command command) {
		this.conn = conn;
		this.command = command;
	}

	public String run(Result result) throws Exception {
		// 删除所有与linkPids关联的收费站
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete.Operation(
				this.conn);
		return op.deleteRdTollgate(result, this.command.getLinkPids());
	}

}
