package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 
 * @ClassName: Process
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:38:48
 * @Description: TODO
 */
public class Process extends AbstractProcess<Command> implements IProcess {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		this.getCommand().setRdObject((RdObject) new AbstractSelector(RdObject.class, getConn()).loadById(this.getCommand().getPid(),true));

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
