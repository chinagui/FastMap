package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
    public Process() {
    }

    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    private Check check=new Check();
    
    public String preCheck() throws Exception{
    	check.PERMIT_CHECK_NO_REPEAT_FEATURE(this.getCommand(), this.getConn());
    	return super.preCheck();
    }
    
    @Override
    public String exeOperation() throws Exception {
        return new Operation(getCommand(), getConn()).run(getResult());
    }
}
