package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGroupSearch;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresGroupSearch search = new ScPlateresGroupSearch(this.getConn());

        this.getCommand().setGroup(search.loadById(getCommand().getGroupId()));

        return true;
    }

    @Override
    public String exeOperation() throws Exception {

        return new Operation(this.getCommand()).run(this.getResult());
    }
}




