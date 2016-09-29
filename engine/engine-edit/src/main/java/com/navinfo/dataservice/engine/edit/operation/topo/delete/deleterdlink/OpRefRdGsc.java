package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;

public class OpRefRdGsc implements IOperation {
	
	private Command command;

	public OpRefRdGsc(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		for( RdGsc rdGsc : command.getRdGscs()){
			if(rdGsc.getLinks().size() <=2)
			{
				result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());
			}
			else
			{
				for(IRow row : rdGsc.getLinks())
				{
					RdGscLink gscLink = (RdGscLink) row;
					
					if(gscLink.getLinkPid() == command.getLinkPid())
					{
						result.insertObject(gscLink, ObjStatus.DELETE, gscLink.getPid());
					}
				}
			}
		}
		
		return null;
	}
}
