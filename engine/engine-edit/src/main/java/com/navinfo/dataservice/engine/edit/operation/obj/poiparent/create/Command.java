package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * 
* @Title: Command.java 
* @Description: 前台创建poiparent request数据封装类
* @author 鹿尧
* @date 2016年6月17日  
* @version V1.0
 */
public class Command extends AbstractCommand implements ICommand {

	private String requester;

	/**
	 * 子poi的pid
	 */
	private Integer objId;	
	
	/**
	 * 父poi的pid
	 */
	private Integer parentPid;


	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public Integer getParentPid() {
		return parentPid;
	}

	public void setParentPid(Integer parentPid) {
		this.parentPid = parentPid;
	}	

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.IXPOIEVENT;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		
		this.setDbId(json.getInt("dbId"));
		
		this.setObjId(json.getInt("objId"));
		
		this.setParentPid(json.getInt("parentPid"));
		
	}

}
