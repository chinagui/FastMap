package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdcross.delete;

import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;

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
