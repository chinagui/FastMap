package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.update;

import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;


public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {

		ScPlateresLinkSearch search = new ScPlateresLinkSearch(this.getConn());

		this.getCommand().setLink(search.loadById(this.getCommand().getGemetryId()));

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
