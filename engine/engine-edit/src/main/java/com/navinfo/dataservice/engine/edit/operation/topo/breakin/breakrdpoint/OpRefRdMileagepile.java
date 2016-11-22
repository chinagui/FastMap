package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.List;

public class OpRefRdMileagepile {

    private Connection conn;

    public OpRefRdMileagepile(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, int linkPid, List<RdLink> newLinks) throws Exception {
        // 维护里程桩
        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update.Operation(this.conn);
        operation.breakRdLink(linkPid, newLinks, result);
        return null;
    }
}
