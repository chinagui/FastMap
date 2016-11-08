package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.move;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.selector.rd.hgwg.RdHgwgLimitSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/8 0008.
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
    public boolean prepareData() throws Exception {
        RdHgwgLimitSelector selector = new RdHgwgLimitSelector(this.getConn());
        RdHgwgLimit hgwgLimit = (RdHgwgLimit) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
        this.getCommand().setHgwgLimit(hgwgLimit);
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }
}
