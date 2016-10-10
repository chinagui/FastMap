/**
 * 
 */
package com.navinfo.dataservice.dao.glm.iface;

import net.sf.json.JSONObject;

/** 
 * 封装的提示类
* @ClassName: AlertObject 
* @author Zhang Xiaolong
* @date 2016年9月12日 下午5:05:52 
* @Description: TODO
*/
public class AlertObject {
	//对象类型
	private ObjType objType;
	
	//对象pid
	private int pid;
	
	//对象状态
	private ObjStatus status;

	public ObjType getObjType() {
		return objType;
	}

	public void setObjType(ObjType objType) {
		this.objType = objType;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public ObjStatus getStatus() {
		return status;
	}

	public void setStatus(ObjStatus status) {
		this.status = status;
	}
	
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JSONObject json = JSONObject.fromObject(this);

		return json;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof AlertObject)
		{
			AlertObject compAlert = (AlertObject) obj;
			
			if(compAlert.getPid() == this.getPid() && compAlert.getObjType().equals(this.getObjType()) && compAlert.getStatus().equals(this.getStatus()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
