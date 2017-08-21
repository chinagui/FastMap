package com.navinfo.dataservice.engine.edit.operation.obj.rdlinkwarning.delete;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning.RdLinkWarning;
import com.navinfo.dataservice.dao.glm.selector.rd.rdlinkwarning.RdLinkWarningSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * Created by ly on 2017/8/18.
 */
public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    public boolean prepareData() throws Exception {

        RdLinkWarningSelector selector = new RdLinkWarningSelector(
                this.getConn());

        RdLinkWarning rdLinkWarning = (RdLinkWarning) selector.loadById(this
                .getCommand().getPid(), true);

        this.getCommand().setRdLinkWarning(rdLinkWarning);

        return true;
    }

    @Override
    public String exeOperation() throws Exception {

        IOperation op = new Operation(this.getCommand());

        return op.run(this.getResult());
    }

}
