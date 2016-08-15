package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * 
 * @Title: OpRefRdSpeedbump.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 下午2:14:39
 * @version: v1.0
 */
public class OpRefRdSpeedbump implements IOperation {

	private Command command;

	private Connection conn;

	public OpRefRdSpeedbump(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete.Operation(
				this.conn);
		return op.deleteSpeedbump(result, command.getLinkPids());
	}

}
