/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;

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
	
	public String run(Result result,ZoneLink link) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
				this.conn);
		rdinterOperation.deleteByLink(link.getsNodePid(),link.geteNodePid(),"ZONE_NODE", result);
		
		return null;
	}

}
