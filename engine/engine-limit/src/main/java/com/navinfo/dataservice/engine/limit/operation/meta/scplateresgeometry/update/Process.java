package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;

public class Process extends AbstractProcess<Command> {
    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresGeometrySearch search = new ScPlateresGeometrySearch(this.getConn());

        this.getCommand().setGeometry(search.loadById(getCommand().getId()));

        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }
}
