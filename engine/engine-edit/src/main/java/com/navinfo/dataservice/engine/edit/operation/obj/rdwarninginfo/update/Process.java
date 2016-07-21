package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.update;


import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.dao.glm.selector.rd.warninginfo.RdWarninginfoSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {


	public Process(AbstractCommand command) throws Exception {
		super(command);
	}



	@Override
	public boolean prepareData() throws Exception {

		RdWarninginfoSelector selector = new RdWarninginfoSelector(this.getConn());

		RdWarninginfo rdWarninginfo = (RdWarninginfo) selector.loadById(this.getCommand().getPid(),
				true);
		
		this.getCommand().setRdWarninginfo(rdWarninginfo);
		
		return true;
	}

	
	@Override
	public String exeOperation() throws Exception {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand()).run(this.getResult());
	}
}