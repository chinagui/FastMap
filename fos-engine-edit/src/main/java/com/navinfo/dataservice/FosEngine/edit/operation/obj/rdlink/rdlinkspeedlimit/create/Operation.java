package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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

			if (mapLinks.get(linkPid).getDirect() == 2) {
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
			
			result.insertObject(limit, ObjStatus.INSERT);
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
