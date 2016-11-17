package com.navinfo.dataservice.engine.edit.operation.obj.tmc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;


public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	public Operation(Command command, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = create(result);

		return msg;
	}

	private String create(Result result) throws Exception {

		RdTmclocation tmclocation = new RdTmclocation();

		tmclocation.setPid(PidUtil.getInstance().applyRdTmcLocationPid());
		
		tmclocation.setTmcId(this.command.getTmcId());
		
		tmclocation.setLoctableId(this.command.getLoctableId());

		List<IRow> links = new ArrayList<IRow>();
		
		int locDirect = this.command.getLocDirect();
		
		int direct = this.command.getDirect();
		
		RdLinkSelector selector = new RdLinkSelector(conn);
		
		List<IRow> linkList = selector.loadByIds(this.command.getLinkPids(), true, false);
		
		RdLink firstLink = (RdLink) linkList.get(0);
		
		int inNodePid = 0;
		
		if(direct == 1)
		{
			inNodePid = firstLink.geteNodePid();
		}
		else if(direct == 2)
		{
			inNodePid = firstLink.getsNodePid();
		}
		
		for (int i=0;i<linkList.size();i++) {

			RdLink link = (RdLink) linkList.get(i);
			
			RdTmclocationLink tmcLocationLink = new RdTmclocationLink();

			tmcLocationLink.setLinkPid(link.getPid());
			
			tmcLocationLink.setLocDirect(locDirect);
			
			tmcLocationLink.setDirect(direct);
			
			if(i>0 && inNodePid!=0)
			{
				if(link.getsNodePid() == inNodePid)
				{
					//如果作用方向和该link的起点到终点的划线方向一致，则赋值为'1'
					tmcLocationLink.setDirect(1);
					
					//该link的终点作为下一个link的进入点
					inNodePid = link.geteNodePid();
				}
				else if(link.geteNodePid() == inNodePid)
				{
					//如果作用方向和该link的起点到终点的划线方向相反，则赋值为'2'
					tmcLocationLink.setDirect(2);
					
					//该link的起点作为下一个link的进入点
					inNodePid = link.getsNodePid();
				}
				
			}
			
			tmcLocationLink.setGroupId(tmclocation.getPid());
			
			links.add(tmcLocationLink);
		}

		tmclocation.setLinks(links);

		result.insertObject(tmclocation, ObjStatus.INSERT, tmclocation.pid());

		return null;
	}

}
