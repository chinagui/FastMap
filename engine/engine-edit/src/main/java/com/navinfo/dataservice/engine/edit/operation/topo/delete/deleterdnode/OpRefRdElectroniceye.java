/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * 
 * @Title: OpRefRdElectroniceye.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月2日 下午2:41:41
 * @version: v1.0
 */
public class OpRefRdElectroniceye {

	private Connection conn;

	private Command command;

	public OpRefRdElectroniceye(Connection conn, Command command) {
		this.conn = conn;
		this.command = command;
	}

	public String run(Result result) throws Exception {
		// 删除所有与linkPids关联的电子眼
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Operation(
				this.conn);
		return op.deleteRelectroniceye(result, this.command.getLinkPids());
	}

}
