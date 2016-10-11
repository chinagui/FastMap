package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
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

	private Check check = new Check(this.getCommand());

	@Override
	public boolean prepareData() throws Exception {
		RdInter inter = (RdInter) new RdInterSelector(getConn()).loadById(this.getCommand().getPid(), true);
		
		this.getCommand().setRdInter(inter);
		
		//check.hasNodeIsInter(getConn(),inter);
		
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
