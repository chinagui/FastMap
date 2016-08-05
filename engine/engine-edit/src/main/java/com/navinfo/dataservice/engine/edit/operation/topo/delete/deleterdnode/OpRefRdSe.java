/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * 
 * @Title: OpRefRdSe.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月4日 下午5:53:15
 * @version: v1.0
 */
public class OpRefRdSe {

	private Connection conn;

	private Command command;

	public OpRefRdSe(Connection conn, Command command) {
		this.conn = conn;
		this.command = command;
	}

	public String run(Result result) throws Exception {
		// 删除所有与linkPids关联的分岔路提示
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete.Operation(
				this.conn);
		return op.deleteRdSe(result, this.command.getLinkPids());
	}

}
