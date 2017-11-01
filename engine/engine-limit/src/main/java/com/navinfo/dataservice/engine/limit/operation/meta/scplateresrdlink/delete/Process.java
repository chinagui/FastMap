package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {

		ScPlateresRdlinkSearch search = new ScPlateresRdlinkSearch(this.getConn());

		List<ScPlateresRdLink> rdlinks = new ArrayList<>();

		for (Integer linkPid : this.getCommand().getMapping().keySet()) {

			List<ScPlateresRdLink> links = search.loadByLinkPId(linkPid);

			for (ScPlateresRdLink link : links) {

				this.getCommand().getMapping().get(linkPid).contains(link.getGeometryId());

				rdlinks.add(link);
			}
		}

		this.getCommand().setLinks(rdlinks);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getConn(), this.getCommand());

		operation.run(getResult());

		return null;
	}

}
