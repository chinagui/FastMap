package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

import java.sql.Connection;
import java.util.List;
/***
 * 
 * @author zhaokk
 *
 */
public class OpRefRdlane {
    private Connection conn;

    public OpRefRdlane(Connection conn) {
        this.conn = conn;
    }

    public void updateRelation(Command command, List<RdLink> newLinks, Result result) throws Exception {
    	// 详细车道维护
        com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation rdlaneOperation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(this.conn);
        rdlaneOperation.breakRdLink(command.getRdLink().getPid(), newLinks, result);

    }
}
