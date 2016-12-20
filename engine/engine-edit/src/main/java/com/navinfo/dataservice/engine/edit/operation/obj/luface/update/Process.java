package com.navinfo.dataservice.engine.edit.operation.obj.luface.update;

import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @Title: Process.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月30日 上午9:58:04
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
		LuFaceSelector selector = new LuFaceSelector(this.getConn());
		LuFace face = (LuFace) selector.loadById(this.getCommand().getContent().getInt("pid"), true);
		this.getCommand().setFace(face);
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), getConn()).run(this.getResult());
	}

}
