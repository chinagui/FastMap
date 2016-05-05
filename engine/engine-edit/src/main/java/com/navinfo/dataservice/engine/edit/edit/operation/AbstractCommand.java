package com.navinfo.dataservice.engine.edit.edit.operation;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
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
	private List<IRow> glmList;
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public List<IRow> getGlmList() {
		return glmList;
	}
	public void setGlmList(List<IRow> glmList) {
		this.glmList = glmList;
	}
}
