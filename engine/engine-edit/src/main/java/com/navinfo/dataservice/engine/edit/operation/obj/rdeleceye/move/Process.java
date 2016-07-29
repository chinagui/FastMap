package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;

import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年7月29日 下午3:45:06
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
		// 根据EleceyePid加载需要更新的RdElectroniceye
		this.getCommand().setEleceye((RdElectroniceye) new RdElectroniceyeSelector(this.getConn())
				.loadById(this.getCommand().getPid(), true));
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		new Operation(this.getCommand()).run(this.getResult());
		return null;
	}

}
