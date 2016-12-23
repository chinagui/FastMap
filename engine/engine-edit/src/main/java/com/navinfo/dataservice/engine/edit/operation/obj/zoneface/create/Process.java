package com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
/**
 * 
 * @author zhaokk
 * ZONE 面执行类
 *
 */
public class Process extends AbstractProcess<Command> implements IProcess {
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		if(this.getCommand().getLinkPids() != null){
			List<IObj> links = new ArrayList<IObj>();
			if (this.getCommand().getLinkType().equals(ObjType.ZONELINK.toString())) {
			ZoneLinkSelector zoneLinkSelector = new ZoneLinkSelector(this.getConn());
			
			for (int linkPid :this.getCommand().getLinkPids()){
				ZoneLink link =(ZoneLink)zoneLinkSelector.loadById(linkPid, true);
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
		return new Operation(this.getCommand(),this.getResult()).run(this.getResult());
	}

	

}
