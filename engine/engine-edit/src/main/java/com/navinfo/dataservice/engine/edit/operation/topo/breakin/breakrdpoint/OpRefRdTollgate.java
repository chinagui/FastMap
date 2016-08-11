package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * 
 * @Title: OpRefRdTollgate.java
 * @Description: 维护打断时收费站信息
 * @author zhangyt
 * @date: 2016年8月11日 上午10:23:06
 * @version: v1.0
 */
public class OpRefRdTollgate {

	private Connection conn;

	public OpRefRdTollgate() {
	}

	public OpRefRdTollgate(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
		// 维护收费站
		com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update.Operation(
				this.conn);
		return op.breakRdTollgate(result, oldLinkPid, newLinks);
	}
}
