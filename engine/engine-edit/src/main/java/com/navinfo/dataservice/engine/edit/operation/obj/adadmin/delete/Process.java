package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command> implements IProcess {
	private AdAdmin adAdmin;
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

		AdAdminSelector selector = new AdAdminSelector(this.getConn());

		this.adAdmin = (AdAdmin) selector.loadById(this.getCommand().getPid(),
				true);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), this.adAdmin,getConn()).run(this.getResult()); 
	}

}
