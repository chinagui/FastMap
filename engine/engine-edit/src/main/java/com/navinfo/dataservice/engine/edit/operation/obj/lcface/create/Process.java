package com.navinfo.dataservice.engine.edit.operation.obj.lcface.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

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
		if (this.getCommand().getLinkPids() != null) {
			List<IObj> links = new ArrayList<IObj>();
			LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());

			for (int linkPid : this.getCommand().getLinkPids()) {
				LcLink link = (LcLink) lcLinkSelector.loadById(linkPid, true);
				links.add(link);
			}
			this.getCommand().setLinks(links);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#
	 * createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.check, this.getConn(), this.getResult()).run(this.getResult());
	}

}
