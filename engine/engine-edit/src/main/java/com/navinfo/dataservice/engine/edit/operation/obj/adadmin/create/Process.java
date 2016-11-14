package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {
        RdLinkSelector selector = new RdLinkSelector(getConn());
        RdLink link = (RdLink) selector.loadById(getCommand().getLinkPid(), true);
        getCommand().setLink(link);
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        // TODO Auto-generated method stub
        return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
    }

}
