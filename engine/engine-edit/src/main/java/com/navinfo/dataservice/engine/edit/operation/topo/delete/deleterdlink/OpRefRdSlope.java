/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;

/** 
* @ClassName: OpRefRdSlope 
* @author 赵凯凯
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefRdSlope{
	
	private Connection conn;

	public OpRefRdSlope(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,int linkPid) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation rdSlopeOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete.Operation(
				conn);

		rdSlopeOperation.deleteByLink(result, linkPid);
		
		return null;
	}

}
