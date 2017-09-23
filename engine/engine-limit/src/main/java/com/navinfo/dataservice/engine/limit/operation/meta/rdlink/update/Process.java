package com.navinfo.dataservice.engine.limit.operation.meta.rdlink.update;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.meta.rdlink.update.Command;
import com.navinfo.dataservice.engine.limit.operation.meta.rdlink.update.Operation;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

    @Override
    public boolean prepareData() throws Exception {

    	ScPlateresRdlinkSearch search = new ScPlateresRdlinkSearch(this.getConn());

        this.getCommand().setRdLink(search.loadById(this.getCommand().getLinkpid()));

        return true;
    }
    
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
}
