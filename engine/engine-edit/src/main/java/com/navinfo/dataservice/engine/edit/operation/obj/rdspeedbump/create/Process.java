package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:09:35
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

	public Process() {
	}

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
