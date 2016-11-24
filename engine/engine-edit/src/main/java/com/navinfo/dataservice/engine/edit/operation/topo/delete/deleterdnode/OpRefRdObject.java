/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @ClassName: OpRefTrafficsignal
 * @author Zhang Xiaolong
 * @date 2016年7月21日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdObject {

	private Connection conn;

	public OpRefRdObject(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, Command command) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
				this.conn);
		rdinterOperation.deleteByType(command.getLinkPids(), ObjType.RDLINK, result);
		return null;
	}

}
