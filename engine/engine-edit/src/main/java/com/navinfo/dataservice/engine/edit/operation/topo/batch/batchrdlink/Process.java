package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/23 0023.
 */
public class Process extends AbstractProcess<Command> {
    public Process() {
    }

    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public String run() throws Exception {
        String msg = null;
        getConn().setAutoCommit(false);
        try {
            msg = exeOperation();
            recordData();
            getConn().commit();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                getConn().close();
            } catch (Exception e) {
            }
        }
        return msg;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
