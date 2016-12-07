package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.relation;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/21 0021.
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
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
