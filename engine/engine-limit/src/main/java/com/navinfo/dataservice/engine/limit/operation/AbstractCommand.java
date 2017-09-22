package com.navinfo.dataservice.engine.limit.operation;

import com.navinfo.dataservice.engine.limit.glm.iface.ICommand;

public abstract class AbstractCommand implements ICommand {
	private int dbId;
	private long userId;
	private int taskId;
	private int infect;

	public int getInfect() {
		return infect;
	}

	public void setInfect(int infect) {
		this.infect = infect;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	private boolean hasConn = false;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
}
