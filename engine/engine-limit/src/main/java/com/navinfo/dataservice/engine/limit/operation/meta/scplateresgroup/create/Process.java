package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.create;

import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

public class Process extends AbstractProcess<Command>{

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
    }
}
