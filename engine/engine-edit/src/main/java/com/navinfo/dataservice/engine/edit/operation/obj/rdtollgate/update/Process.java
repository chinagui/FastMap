package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update;

import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午3:30:33
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
		RdTollgateSelector selector = new RdTollgateSelector(this.getConn());
		RdTollgate tollgate = (RdTollgate) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
		this.getCommand().setTollgate(tollgate);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
