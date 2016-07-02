package com.navinfo.dataservice.engine.dropbox.dao;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

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
		return ObjType.IXPOIPHOTO;
	}

}
