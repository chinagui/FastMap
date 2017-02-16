package com.navinfo.dataservice.dao.check;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

public class Command implements ICommand {
	private OperType operType =  OperType.CREATE;

	public void setOperType(OperType operType) {
		this.operType = operType;
	}

	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return operType;
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
