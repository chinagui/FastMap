package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {
	private Check check = new Check();
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {
		if(this.getCommand().getLinkPids() != null){
			if (this.getCommand().getLinkType().equals(ObjType.ADLINK.toString())) {
			AdLinkSelector adLinkSelector = new AdLinkSelector(this.getConn());
			List<AdLink> adLinks = new ArrayList<AdLink>();
			for (int linkPid :this.getCommand().getLinkPids()){
				AdLink link =(AdLink)adLinkSelector.loadById(linkPid, true);
				adLinks.add(link);
			}
			this.getCommand().setLinks(adLinks);
		}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.check, this.getConn(),this.getResult());
	}

	

}
