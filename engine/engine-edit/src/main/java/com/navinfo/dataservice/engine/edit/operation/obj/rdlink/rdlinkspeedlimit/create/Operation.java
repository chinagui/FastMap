package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class Operation implements IOperation {

	private Command command;

	private RdSpeedlimit rdSpeedlimit;

	private List<RdLink> rdLinks;

	private Map<Integer, RdLink> mapLinks;

	public Operation(Command command, RdSpeedlimit rdSpeedlimit,
			List<RdLink> rdLinks) {
		this.command = command;

		this.rdSpeedlimit = rdSpeedlimit;

		this.rdLinks = rdLinks;

		mapLinks = new HashMap<Integer, RdLink>();

		for (RdLink link : rdLinks) {
			mapLinks.put(link.getPid(), link);
		}
	}

	@Override
	public String run(Result result) throws Exception {
		
		this.orginzeLinkDirect();
		
		JSONArray linkPids = command.getLinkPids();
		
		for(int i=0;i<linkPids.size();i++){
			int linkPid = linkPids.getInt(i);
			
			RdLinkSpeedlimit limit = new RdLinkSpeedlimit();
			
			limit.setLinkPid(linkPid);
			
			limit.setSpeedType(rdSpeedlimit.getSpeedType());
			
			RdLink link = mapLinks.get(linkPid);
			
			limit.setMesh(link.getMeshId());

			if (link.getDirect() == 2) {
				limit.setFromSpeedLimit(rdSpeedlimit.getSpeedValue());
				limit.setToSpeedLimit(0);
				
				limit.setFromLimitSrc(rdSpeedlimit.getLimitSrc());
				
				limit.setToLimitSrc(0);
			}else{
				limit.setFromSpeedLimit(0);
				limit.setToSpeedLimit(rdSpeedlimit.getSpeedValue());
				
				limit.setFromLimitSrc(0);
				
				limit.setToLimitSrc(rdSpeedlimit.getLimitSrc());
			}
			
			
			limit.setSpeedClass(0);
			
			limit.setSpeedDependent(rdSpeedlimit.getSpeedDependent());
			
			limit.setTimeDomain(rdSpeedlimit.getTimeDomain());
			
			limit.setSpeedClassWork(0);
			
			result.insertObject(limit, ObjStatus.INSERT, linkPid);
		}

		return null;
	}

	private void orginzeLinkDirect() {

		int preEnodePid = 0;
		
		RdLink link = rdLinks.get(0);

		if (rdSpeedlimit.getDirect() == 3) {
			

			int tmpNodePid = link.getsNodePid();

			link.setsNodePid(link.geteNodePid());

			link.seteNodePid(tmpNodePid);
			
			link.setDirect(3);

		}else{
			link.setDirect(2);
		}
		
		preEnodePid = link.geteNodePid();

		for (int i = 1; i < rdLinks.size(); i++) {
			link = rdLinks.get(i);

			if (preEnodePid == link.geteNodePid()) {

				int tmpNodePid = link.getsNodePid();

				link.setsNodePid(link.geteNodePid());

				link.seteNodePid(tmpNodePid);

				link.setDirect(3);

			}else{
				link.setDirect(2);
			}
			
			preEnodePid = link.geteNodePid();
			
		}

	}

}
