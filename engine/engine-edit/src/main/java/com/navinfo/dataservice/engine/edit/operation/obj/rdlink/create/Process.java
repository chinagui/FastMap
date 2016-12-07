package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }
    
    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    private Check check = new Check();

    @Override
    public String preCheck() throws Exception {
        check.checkDupilicateNode(this.getCommand().getGeometry());
        check.checkGLM04002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
        check.checkGLM13002(this.getConn(), this.getCommand().geteNodePid(), this.getCommand().getsNodePid());
        return super.preCheck();
    }

    @Override
    public void postCheck() throws Exception {
        check.postCheck(this.getConn(), this.getResult(), this.getCommand().getDbId());
        super.postCheck();
    }

    @Override
    public String exeOperation() throws Exception {
        Operation operation = new Operation(this.getCommand(), check, this.getConn());
        String msg = operation.run(this.getResult());
        return msg;
    }
}
