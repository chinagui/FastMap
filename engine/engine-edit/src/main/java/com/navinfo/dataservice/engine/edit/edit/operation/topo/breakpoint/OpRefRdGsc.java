package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;

public class OpRefRdGsc implements IOperation {
	
	private Command command;
	
	private Result result;

	public OpRefRdGsc(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {

		this.result = result;
		
		List<RdGsc> rdGscList = command.getRdGscs();
		
		if(CollectionUtils.isNotEmpty(rdGscList))
		{
			this.handleRdGsc(rdGscList);
		}

		return null;
	}

	// 处理立交
	private void handleRdGsc(List<RdGsc> list) throws Exception {

		for (RdGsc rr : list) {
			
			List<IRow> rdGscLinkList = rr.getLinks();
			
			//每个立交至少由两条线组成，循环遍历每条link
			for(IRow row : rdGscLinkList)
			{
				RdGscLink rdGscLink = (RdGscLink) row;
				
				Map<String, Object> changedFields = rdGscLink.changedFields();
				
				//找到打断的那条link
				if(rdGscLink.getPid() == command.getLinkPid())
				{
					//当立交和新生成的link距离为0，代表该新生成的link为立交的组成link
					if(rr.getGeometry().distance(command.getLink1().getGeometry()) == 0)
					{
						changedFields.put("linkPid", command.getLink1().getPid());
						
						result.insertObject(rdGscLink, ObjStatus.UPDATE, rr.pid());
					}
				}
			}
		}
	}

}
