package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private RdSpeedlimit rdSpeedlimit;

	private List<RdLink> rdLinks;

	@Override
	public boolean prepareData() throws Exception {

		RdSpeedlimitSelector slSelector = new RdSpeedlimitSelector(this.getConn());
		
		rdSpeedlimit = (RdSpeedlimit) slSelector.loadById(this.getCommand().getPid(), true);
		
		RdLinkSelector rdLinkSelector = new RdLinkSelector(this.getConn());
		
		String path = slSelector.trackSpeedLimitLink(rdSpeedlimit.getLinkPid(), rdSpeedlimit.getDirect());
		
		String[] splits = path.split(",");
		
		rdLinks = new ArrayList<RdLink>();
		
		for(String str : splits){
			rdLinks.add((RdLink) rdLinkSelector.loadById(Integer.parseInt(str), true));
		}
		
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),rdSpeedlimit,rdLinks).run(this.getResult());
	}
	

}
