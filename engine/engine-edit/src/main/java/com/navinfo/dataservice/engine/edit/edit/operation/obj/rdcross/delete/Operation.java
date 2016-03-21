package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdcross.delete;

import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class Operation implements IOperation {

	private Command command;

	private RdCross cross;

	public Operation(Command command, RdCross cross) {
		this.command = command;

		this.cross = cross;

	}

	@Override
	public String run(Result result) throws Exception {

		result.getDelObjects().add(cross);
				
		return null;
	}

}
