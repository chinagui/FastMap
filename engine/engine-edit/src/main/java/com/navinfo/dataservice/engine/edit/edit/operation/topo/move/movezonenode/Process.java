package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.movezonenode;

import java.util.List;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

/**
 * @author zhaokk
 * ZONE点具体执行类
 */
public class Process extends AbstractProcess<Command> {
	
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

 /*
 * 移动Zone点加载对应的Zone线信息
 */
	public void lockZoneLink() throws Exception {

		ZoneLinkSelector selector = new ZoneLinkSelector(this.getConn());

		List<ZoneLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		this.getCommand().setLinks(links);
	}
	
	 /*
	 * 移动行政区划点加载对应的行政区点线信息
	 */
	public void lockZoneNode() throws Exception {
		 ZoneNodeSelector nodeSelector = new ZoneNodeSelector(this.getConn());
		 this.getCommand().setZoneNode( (ZoneNode) nodeSelector.loadById(this.getCommand().getNodePid(), true));
		}
	
	 /*
		 * 移动行政区划点加载对应的行政区点面信息
		 */
    public void lockZoneFace() throws Exception {
		ZoneFaceSelector faceSelector = new ZoneFaceSelector(this.getConn());
		this.getCommand().setFaces(faceSelector.loadZoneFaceByNodeId(this.getCommand().getNodePid(), true));
				
	}
		
	@Override
	public boolean prepareData() throws Exception {
		
		this.lockZoneNode();
		this.lockZoneLink();
		this.lockZoneFace();
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
