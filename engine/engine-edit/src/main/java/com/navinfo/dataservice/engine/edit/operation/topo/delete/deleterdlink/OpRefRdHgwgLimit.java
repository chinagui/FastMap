package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class OpRefRdHgwgLimit implements IOperation {

    private Command command;

    private Connection conn;

    public OpRefRdHgwgLimit(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Operation op = new com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete.Operation(
                this.conn);
        List<Integer> linkPids = new ArrayList<Integer>();
        linkPids.add(command.getLinkPid());
        return op.deleteHgwgLimit(result, linkPids);
    }

}
