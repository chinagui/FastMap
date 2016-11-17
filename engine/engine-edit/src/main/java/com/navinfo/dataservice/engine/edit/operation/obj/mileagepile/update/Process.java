package com.navinfo.dataservice.engine.edit.operation.obj.mileagepile.update;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.mileagepile.RdMileagepile;
import com.navinfo.dataservice.dao.glm.selector.rd.mileagepile.RdMileagepileSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/9 0009.
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
        RdMileagepileSelector selector = new RdMileagepileSelector(this.getConn());
        RdMileagepile mileagepile = (RdMileagepile) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
        this.getCommand().setMileagepile(mileagepile);
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }
}
