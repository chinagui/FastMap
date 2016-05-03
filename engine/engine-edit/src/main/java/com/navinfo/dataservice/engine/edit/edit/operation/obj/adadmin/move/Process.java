package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.move;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command>   implements IProcess {
	private AdAdmin moveAdmin;
	
	/**
	 * @param command
	 * @throws Exception
	 */
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean prepareData() throws Exception {
		AdAdminSelector adAdminSelector = new AdAdminSelector(this.getConn());
		
		this.moveAdmin = (AdAdmin) adAdminSelector.loadById(this.getCommand().getPid(), true);
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public IOperation createOperation() {
		return new Operation(this.getCommand(),moveAdmin);
	}

}
