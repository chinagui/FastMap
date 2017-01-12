package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 
 * @ClassName: Process
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:42
 * @Description: TODO
 */
public class Process extends AbstractProcess<Command> {
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
	
	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub
		// this.createPostCheckGlmList();
		List<IRow> glmList = new ArrayList<IRow>();
		glmList.addAll(this.getResult().getAddObjects());
		glmList.addAll(this.getResult().getUpdateObjects());
		glmList.addAll(this.getResult().getDelObjects());
		this.checkCommand.setGlmList(glmList);
		this.checkEngine.postCheck();

	}

}
