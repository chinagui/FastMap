package com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {
		check.checkDupilicateNode(this.getCommand().getGeometry());
		
		//TODO
		//相邻形状点不可过近，不能小于2m
		
		//创建link，鼠标只点一个形状点就进行保存时，不能保存数据
		return super.preCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		return msg;
	}
	
}
