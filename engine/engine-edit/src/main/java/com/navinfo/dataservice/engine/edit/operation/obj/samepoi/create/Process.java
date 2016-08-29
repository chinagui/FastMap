package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:28:31
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
