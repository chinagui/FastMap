package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.create;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * Created by ly on 2017/8/18.
 */
public class Process extends AbstractProcess<Command> implements IProcess {

    /**
     * @param command
     * @throws Exception
     */
    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
    }
}
