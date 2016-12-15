package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdTrafficsignal {

    private Connection conn;

    public OpRefRdTrafficsignal(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, int linkPid, List<RdLink> newLinks) throws Exception {

        // 维护信号灯
        com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation trafficSignalOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(
                this.conn);
        trafficSignalOperation.breakRdLink(null, linkPid, newLinks, result);

        return null;
    }
}
