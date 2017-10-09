package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.update;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		ScPlateresFaceSearch search = new ScPlateresFaceSearch(this.getConn());

		this.getCommand().setLink(search.loadById(this.getCommand().getGemetryId()));

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
