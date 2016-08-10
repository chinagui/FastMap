/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
* @ClassName: OpRefTrafficsignal 
* @author Zhang Xiaolong
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefRdInter{
	
	private Connection conn;

	public OpRefRdInter(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,RdLink link) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation(
				this.conn);
		rdinterOperation.deleteByLink(link, result);
		
		return null;
	}

}
