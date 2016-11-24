/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
* @ClassName: OpRefTrafficsignal 
* @author Zhang Xiaolong
* @date 2016年7月21日 下午5:03:21 
* @Description: TODO
*/
public class OpRefRdTmcLocation{
	
	private Connection conn;

	public OpRefRdTmcLocation(Connection conn) {
		this.conn = conn;
	}
	
	public String run(Result result,RdLink rdLink) throws Exception {
		
		List<RdLink> rdLinkList = new ArrayList<>();
		
		rdLinkList.add(rdLink);
		
		List<Integer> linkPidList = new ArrayList<>();
		
		linkPidList.add(rdLink.getPid());
		
		//删除link维护tmclocation匹配关系
		com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation rdinterOperation = new com.navinfo.dataservice.engine.edit.operation.obj.tmc.update.Operation(
				this.conn);

		rdinterOperation.deleteLinkUpdateTmc(result,rdLinkList,linkPidList);
		
		return null;
	}

}
