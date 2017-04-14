package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.sql.Connection;

/**
 * @Title: Process
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
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
        CmgBuildface cmgface = (CmgBuildface) new AbstractSelector(CmgBuildface.class, getConn()).
                loadById(getCommand().getCmgface().pid(), false);
        getCommand().setCmgface(cmgface);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand()).run(getResult());
    }
}
