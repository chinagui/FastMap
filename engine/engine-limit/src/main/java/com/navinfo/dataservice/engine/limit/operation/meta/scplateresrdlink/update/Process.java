package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update.Command;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update.Operation;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresRdlinkSearch;

import java.util.ArrayList;
import java.util.List;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
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
		return new Operation(this.getCommand()).run(this.getResult());
	}
}
