package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete;

import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.selector.rd.speedbump.RdSpeedbumpSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:17:49
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

	public Process() {
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {
		RdSpeedbumpSelector selector = new RdSpeedbumpSelector(this.getConn());
		RdSpeedbump speedbump = (RdSpeedbump) selector.loadById(this.getCommand().getPid(), true);
		this.getCommand().setRdSpeedbump(speedbump);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
