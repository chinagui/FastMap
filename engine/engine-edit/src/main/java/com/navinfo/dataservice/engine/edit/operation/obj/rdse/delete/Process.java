package com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete;

import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.selector.rd.se.RdSeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:45:21
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

	@Override
	public boolean prepareData() throws Exception {
		RdSeSelector rdSeSelector = new RdSeSelector(this.getConn());
		this.getCommand().setRdSe((RdSe) rdSeSelector.loadById(this.getCommand().getPid(), true));
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
