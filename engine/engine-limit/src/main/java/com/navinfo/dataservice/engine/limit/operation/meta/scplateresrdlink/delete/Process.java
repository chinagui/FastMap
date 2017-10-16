package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub

		ScPlateresRdlinkSearch search = new ScPlateresRdlinkSearch(this.getConn());
		List<ScPlateresRdLink> links = new ArrayList<>();

		if (this.getCommand().getLinkpids() != null && this.getCommand().getLinkpids().size() != 0) {
			links.addAll(search.loadByIds(this.getCommand().getLinkpids()));
		}

		if (this.getCommand().getGeometryIds() != null && this.getCommand().getGeometryIds().size() != 0) {
			links = search.loadByGeometryIds(this.getCommand().getGeometryIds());
		}

		this.getCommand().setRdLinks(links);
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getConn(),this.getCommand());
		
		operation.run(getResult());
		
		return null;
	}

}
