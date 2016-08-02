package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @Title: OpRefRdElectroniceye.java
 * @Description: 维护打断时电子眼的关系
 * @author zhangyt
 * @date: 2016年8月2日 下午1:36:52
 * @version: v1.0
 */
public class OpRefRdElectroniceye {

	private Connection conn;

	public OpRefRdElectroniceye() {
	}

	public OpRefRdElectroniceye(Connection conn) {
		this.conn = conn;
	}

	public String run(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
		// 维护电子眼信息breakRdLink
		com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Operation(
				this.conn);
		return op.breakRdLink(result, oldLinkPid, newLinks);
	}
}
