/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
* @ClassName: OpRefTrafficsignal 
* @author Zhang Xiaolong
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefRdSameNode{
	
	private Connection conn;

	public OpRefRdSameNode(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,Command command) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
				this.conn);
		for (RdLink link : command.getLinks()) {
			rdinterOperation.deleteByLink(link.getsNodePid(),link.geteNodePid(),"RD_NODE", result);
		}
		
		return null;
	}

}
