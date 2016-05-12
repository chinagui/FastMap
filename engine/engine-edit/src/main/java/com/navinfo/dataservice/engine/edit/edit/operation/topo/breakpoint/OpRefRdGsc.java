package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

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
				
				//找到打断的那条link
				if(rdGscLink.getPid() == command.getLinkPid())
				{
					Geometry link1Geo = command.getLink1().getGeometry();
					
					//当立交和新生成的link距离为0，代表该新生成的link为立交的组成link
					if(rr.getGeometry().distance(link1Geo) == 0)
					{
						//打断后，立交的link和link序号需要重新计算
						int shpSeqNum = calcShpSeqNum(rr.getGeometry(), link1Geo.getCoordinates());
						
						JSONObject updateContent = new JSONObject();

						updateContent.put("shpSeqNum", shpSeqNum);
						
						updateContent.put("linkPid", command.getLink1().getPid());
						
						boolean changed = rdGscLink.fillChangeFields(updateContent);

						if(changed)
						{
							result.insertObject(rdGscLink, ObjStatus.UPDATE, rr.pid());
						}
					}
				}
			}
		}
	}
	
	private int calcShpSeqNum(Geometry gscGeo, Coordinate[] linkCoors) {

		int result = 1;

		Coordinate gscCoor = gscGeo.getCoordinate();

		for (int i = 0; i < linkCoors.length; i++) {
			Coordinate linkCoor = linkCoors[i];

			if (gscCoor.x == linkCoor.x && gscCoor.y == linkCoor.y) {
				result = i;
				break;
			}
		}

		return result;
	}

}
