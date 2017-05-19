package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class OpRefRdSameNode {

    private Connection conn;

    public OpRefRdSameNode(Connection conn) {
        this.conn = conn;
    }

    public String run(Result result, Command command) throws Exception {

        com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete.Operation(
                this.conn);

        List<Integer> nodePids = new ArrayList<>();

        for (RwNode node : command.getNodes()) {

            nodePids.add(node.getPid());
        }

        operation.deleteByLink(nodePids, "RW_NODE", result);

        return null;
    }

}
