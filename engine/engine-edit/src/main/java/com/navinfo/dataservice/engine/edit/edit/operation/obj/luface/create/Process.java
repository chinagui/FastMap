package com.navinfo.dataservice.engine.edit.edit.operation.obj.luface.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {
	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		if(this.getCommand().getLinkPids() != null){
			List<IObj> links = new ArrayList<IObj>();
			if (this.getCommand().getLinkType().equals(ObjType.LULINK.toString())) {
			LuLinkSelector luLinkSelector = new LuLinkSelector(this.getConn());
			
			for (int linkPid :this.getCommand().getLinkPids()){
				LuLink link =(LuLink)luLinkSelector.loadById(linkPid, true);
				links.add(link);
				}
			}
			if(this.getCommand().getLinkType().equals(ObjType.RDLINK.toString())){
				RdLinkSelector rdLinkSelector = new RdLinkSelector(this.getConn());
				for (int linkPid :this.getCommand().getLinkPids()){
					 RdLink link =(RdLink)rdLinkSelector.loadById(linkPid, true);
					 links.add(link);
					}
			}
			this.getCommand().setLinks(links);
		}
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.check, this.getConn(),this.getResult()).run(this.getResult());
	}

	

}
