package com.navinfo.dataservice.engine.limit.operation.limit.scplateresinfo.update;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresInfoSearch;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresInfoSearch search = new ScPlateresInfoSearch(this.getConn());

        this.getCommand().setInfo(search.loadById(getCommand().getInfoIntelId()));

        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }
}