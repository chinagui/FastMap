/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

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
		List<Integer> linkPidList = new ArrayList<>();

		for (RdLink link : command.getLinks()) {
			linkPidList.add(link.getPid());
		}
		rdinterOperation.deleteByType(linkPidList, ObjType.RDLINK, result);
		return null;
	}

}
