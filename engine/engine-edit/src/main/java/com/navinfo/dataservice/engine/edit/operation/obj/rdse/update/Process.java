package com.navinfo.dataservice.engine.edit.operation.obj.rdse.update;

import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:54:02
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
		RdSeSelector rdSeSelector = new RdSeSelector(this.getConn());
		this.getCommand().setRdSe((RdSe) rdSeSelector.loadById(this.getCommand().getContent().getInt("pid"), true));
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
