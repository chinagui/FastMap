package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command>   implements IProcess {
	private AdAdmin moveAdmin;
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		AdAdminSelector adAdminSelector = new AdAdminSelector(this.getConn());
		
		this.moveAdmin = (AdAdmin) adAdminSelector.loadById(this.getCommand().getPid(), true);
		
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),moveAdmin).run(this.getResult());
	}

}
