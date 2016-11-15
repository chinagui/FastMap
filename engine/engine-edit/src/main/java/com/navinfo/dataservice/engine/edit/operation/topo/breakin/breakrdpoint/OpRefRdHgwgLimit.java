package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.List;

public class OpRefRdHgwgLimit {

    private Connection conn;

    public OpRefRdHgwgLimit(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, int linkPid, List<RdLink> newLinks) throws Exception {
        // 维护限高限重
        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.update.Operation(
                this.conn);
        operation.breakRdLink(linkPid, newLinks, result);
        return null;
    }
}
