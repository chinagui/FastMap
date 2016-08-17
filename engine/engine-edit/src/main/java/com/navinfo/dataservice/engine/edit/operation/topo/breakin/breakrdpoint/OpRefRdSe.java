package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @Title: OpRefRdSe.java
 * @Description: 维护打断时分岔路提示
 * @author zhangyt
 * @date: 2016年8月4日 下午5:47:58
 * @version: v1.0
 */
public class OpRefRdSe {

	private Connection conn;

	public OpRefRdSe() {
	}

	public OpRefRdSe(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
		// 维护分岔路提示
		com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdse.update.Operation(
				this.conn);
		return op.breakRdSe(result, oldLinkPid, newLinks);
	}
}
