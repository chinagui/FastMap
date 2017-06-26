package com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdlink;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * Created by ly on 2017/5/2.
 */
public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {

        super(command);
    }


    @Override
    public String exeOperation() throws Exception {

        return new Operation(getCommand(), getConn()).run(getResult());
    }
}