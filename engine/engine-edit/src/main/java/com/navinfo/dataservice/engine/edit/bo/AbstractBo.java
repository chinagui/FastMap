package com.navinfo.dataservice.engine.edit.bo;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IObj;

public abstract class AbstractBo {
	protected Connection conn;
	protected boolean isLock;
	
	public void insert(){}
	public void update(){}
	public void delete(){}
	public abstract void setPo(IObj po);
	public abstract IObj getPo();
}
