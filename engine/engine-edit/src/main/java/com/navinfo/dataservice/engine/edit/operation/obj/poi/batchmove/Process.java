package com.navinfo.dataservice.engine.edit.operation.obj.poi.batchmove;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	public Process() {
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
