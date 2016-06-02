package com.navinfo.dataservice.dao.check;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

//检查引擎的参数类，调用者将参数输入里面，然后传给检查引擎
public class CheckCommand implements ICommand {
	private OperType operType;
	private ObjType objType;
	private int projectId;
	private List<IRow> glmList;

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
		return objType;
	}
	
	public void setObjType(ObjType objType) {
		this.objType=objType;
	}
	
	public void setOperType(OperType operType) {
		this.operType=operType;
	}
	
	//检查对象list
	public void setGlmList(List<IRow> glmList){
		this.glmList=glmList;
	}
	
	//获取检查对象list
	public List<IRow> getGlmList(){
		return this.glmList;
	}
	
	public void setProjectId(int projectId){this.projectId=projectId;}
	
	public int getProjectId(){
		return this.projectId;
	}
	

}
