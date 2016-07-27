package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
/**
 * 修行ZONE线参数操作类 
 * @author zhaokk
 *
 */
public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public boolean prepareData() throws Exception {
		
		this.getCommand().setUpdateLink((ZoneLink) new ZoneLinkSelector(this.getConn()).loadById(this.getCommand().getLinkPid(), true));
		
		this.getCommand().setFaces(new ZoneFaceSelector(this.getConn()).loadZoneFaceByLinkId(this.getCommand().getLinkPid(), true));
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		//check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
		
		check.checkShapePointDistance(this.getCommand().getLinkGeom());
		
		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
	}

}
