package com.navinfo.dataservice.engine.edit.bo;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IObj;

public abstract class AbstractBo {
	protected Logger log = Logger.getLogger(this.getClass());
	protected Connection conn;
	protected boolean isLock;
	
	public void insert(){}
	public void update(){}
	public void delete(){}
    public abstract AbstractBo copy()throws Exception;
	public abstract void setPo(IObj po);
	public abstract IObj getPo();
}
