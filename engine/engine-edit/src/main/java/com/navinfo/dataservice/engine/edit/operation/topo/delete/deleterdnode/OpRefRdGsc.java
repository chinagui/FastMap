package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;

public class OpRefRdGsc implements IOperation {
	
	private Command command;

	private Connection conn;
	
	public OpRefRdGsc(Command command,Connection conn) {
		this.command = command;
		this.conn = conn;
	}
	
	@Override
	public String run(Result result) throws Exception {

		RdGscSelector selector = new RdGscSelector(conn);
		
		for(Integer linkPid : command.getLinkPids())
		{
			List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(linkPid, "RD_LINK", true);
			
			for(RdGsc gsc: rdGscList)
			{
				//两两立交删除整体
				if(gsc.getLinks().size() <=2)
				{
					result.insertObject(gsc, ObjStatus.DELETE, gsc.getPid());
				}
				else
				{
					//多线立交删除立交组成线
					for(IRow row : gsc.getLinks())
					{
						RdGscLink gscLink = (RdGscLink) row;
						if(gscLink.getLinkPid() == linkPid)
						{
							result.insertObject(gscLink, ObjStatus.DELETE, gscLink.getPid());
						}
					}
				}
			}
		}
		return null;
	}

}
