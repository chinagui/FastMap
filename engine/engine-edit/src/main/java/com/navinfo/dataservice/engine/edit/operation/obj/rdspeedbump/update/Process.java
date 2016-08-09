package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update;

import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月9日 下午4:30:22
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {
	
	public Process() {
		super();
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		RdSpeedbumpSelector selector = new RdSpeedbumpSelector(this.getConn());
		RdSpeedbump speedbump = (RdSpeedbump)selector.loadById(this.getCommand().getContent().getInt("pid"), true);
		this.getCommand().setSpeedbump(speedbump);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
