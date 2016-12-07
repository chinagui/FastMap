package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class OpRefRdMileagepile implements IOperation {

    private Command command;

    private Connection conn;

    public OpRefRdMileagepile(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.delete.Operation(this.conn);
        List<Integer> linkPids = new ArrayList<>();
        linkPids.add(command.getLinkPid());
        return op.deleteRdMileagepile(result, linkPids);
    }

}
