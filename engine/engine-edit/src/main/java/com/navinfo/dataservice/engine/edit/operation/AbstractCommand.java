package com.navinfo.dataservice.engine.edit.operation;

import com.navinfo.dataservice.dao.glm.iface.ICommand;

/** 
 * @ClassName: AbstractCommand
 * @author MaYunFei
 * @date 上午11:05:02
 * @Description: AbstractCommand.java
 */
public abstract class AbstractCommand implements ICommand {
	private int dbId;
	
	private boolean dbFlag;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public boolean getDbFlag() {
		return dbFlag;
	}

	public void setDbFlag(boolean dbFlag) {
		this.dbFlag = dbFlag;
	}
	
}
