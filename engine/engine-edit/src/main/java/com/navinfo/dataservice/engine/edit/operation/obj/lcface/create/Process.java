package com.navinfo.dataservice.engine.edit.operation.obj.lcface.create;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.util.ArrayList;
import java.util.List;

public class Process extends AbstractProcess<Command> implements IProcess {
	private Check check = new Check();

	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		if (this.getCommand().getLinkPids() != null) {
			List<IObj> links = new ArrayList<>();
			if (StringUtils.equals(getCommand().getLinkType(), ObjType.LCLINK.toString())) {
    			LcLinkSelector lcLinkSelector = new LcLinkSelector(this.getConn());
                for (int linkPid : this.getCommand().getLinkPids()) {
                    LcLink link = (LcLink) lcLinkSelector.loadById(linkPid, true);
                    links.add(link);
                }
			} else if (StringUtils.equals(getCommand().getLinkType(), ObjType.RDLINK.toString())) {
                RdLinkSelector rdLinkSelector = new RdLinkSelector(getConn());
                for (int linkPid : this.getCommand().getLinkPids()) {
                    RdLink link = (RdLink) rdLinkSelector.loadById(linkPid, true);
                    links.add(link);
                }
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
		return new Operation(this.getCommand(), this.check, this.getConn(), this.getResult()).run(this.getResult());
	}

}
