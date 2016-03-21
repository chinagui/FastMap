package com.navinfo.dataservice.engine.edit.edit.check;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {

	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjType getObjType() {
		// TODO Auto-generated method stub
		return ObjType.CKEXCEPTION;
	}

}
