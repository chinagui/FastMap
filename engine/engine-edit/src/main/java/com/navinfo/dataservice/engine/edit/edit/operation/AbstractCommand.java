package com.navinfo.dataservice.engine.edit.edit.operation;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

/** 
 * @ClassName: AbstractCommand
 * @author MaYunFei
 * @date 上午11:05:02
 * @Description: AbstractCommand.java
 */
public abstract class AbstractCommand implements ICommand {
	private int projectId;
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
}
