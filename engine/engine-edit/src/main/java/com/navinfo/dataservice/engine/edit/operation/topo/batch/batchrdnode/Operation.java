package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdnode;

import java.sql.Connection;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;

public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {

		JSONArray content = command.getContent();

		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(
				content);

		com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(
				command, result, conn);

		process.innerRun();

		return null;
    }

}
