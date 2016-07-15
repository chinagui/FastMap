package com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Check;
/**
 * @author zhaokk
 * 修改行政区划线参数基础类 
 */
public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}
	public Process(Command command,Result result,Connection conn) throws Exception {
		super(command);
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);

	}

	
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand()).run(this.getResult());

	}
	
	@Override
	public boolean prepareData() throws Exception {
		
		ZoneLinkSelector linkSelector = new ZoneLinkSelector(this.getConn());
		//加载对应AD_LINK信息
		this.getCommand().setZoneLink((ZoneLink)linkSelector.loadById(this.getCommand().getLinkPid(),true)) ;
		return false;
	}


	public String innerRun() throws Exception {
		try {
			this.prepareData();
			String preCheckMsg = this.preCheck();
			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			IOperation operation = new Operation(this.getCommand());
			operation.run(this.getResult());
			this.postCheck();

		} catch (Exception e) {

			getConn().rollback();

			throw e;
		}

		return null;
	}

}
