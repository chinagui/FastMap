package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Command;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Operation;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresManoeuvreSearch search = new ScPlateresManoeuvreSearch(this.getConn());

        this.getCommand().setManoeuvre(search.loadById(getCommand().getManoeuvreId()));

        return true;
    }
    
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
}
