package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.update;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;

import java.util.ArrayList;
import java.util.List;


public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {

		ScPlateresLinkSearch search = new ScPlateresLinkSearch(this.getConn());

		if (this.getCommand().getIds() != null) {
			List<ScPlateresLink> links = new ArrayList<>();

			for (String geometryId : this.getCommand().getIds()) {
				links.add(search.loadById(geometryId));
			}

			this.getCommand().setLinks(links);
		} else {

			this.getCommand().setLink(search.loadById(getCommand().getGemetryId()));
		}
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
