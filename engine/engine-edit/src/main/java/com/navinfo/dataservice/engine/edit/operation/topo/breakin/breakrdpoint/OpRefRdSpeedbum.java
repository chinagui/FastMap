package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @author zhangyt
 * @Title: OpRefRdSpeedbum.java
 * @Description: 维护打断时减速带信息
 * @date: 2016年8月8日 下午1:54:52
 * @version: v1.0
 */
public class OpRefRdSpeedbum {

    private Connection conn;

    public OpRefRdSpeedbum() {
    }

    public OpRefRdSpeedbum(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, int oldLinkPid, List<RdLink> newLinks) throws Exception {
        // 维护减速带
        com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update.Operation(
                this.conn);
        return op.breakSpeedbump(null, result, oldLinkPid, newLinks);
    }
}
