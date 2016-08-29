package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxSamepoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:42:09
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
		IxSamepoiSelector selector = new IxSamepoiSelector(this.getConn());
		IxSamepoi samepoi = (IxSamepoi) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
		this.getCommand().setSamepoi(samepoi);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
