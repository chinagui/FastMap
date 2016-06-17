package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.update;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.AdAdminSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends  AbstractProcess<Command>  implements IProcess {
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
	 * @see com.navinfo.dataservice.engine.edit.edit.operation.Abstractprocess#createOperation()
	 */
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(), this.adAdmin).run(this.getResult());
	}

}
