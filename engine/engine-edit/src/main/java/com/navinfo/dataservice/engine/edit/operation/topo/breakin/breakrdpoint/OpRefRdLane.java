/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @ClassName: OpRefRdLane
 * @author 赵凯凯 打断link维护详细车道信息
 * @date 2016年11月18日 下午5:03:21
 * @Description: TODO
 */
public class OpRefRdLane {

	private Connection conn;

	public OpRefRdLane(Connection conn) {
		this.conn = conn;
	}

	/***
	 * 打断维护详细车道方法
	 * 
	 * @param result
	 * @param linkPid
	 *            打断link的pid
	 * @param newLinks
	 *            打断后新生成的link
	 * @return
	 * @throws Exception
	 */
	public String run(Result result, RdLink link, List<RdLink> newLinks)
			throws Exception {

		com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation rdLaneOperation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
				conn);

		rdLaneOperation.breakRdLink(link, newLinks, result);

		return null;
	}

}
