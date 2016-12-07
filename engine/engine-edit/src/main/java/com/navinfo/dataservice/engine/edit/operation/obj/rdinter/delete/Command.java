package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * 
* @ClassName: Command 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:09 
* @Description: TODO
 */
public class Command extends AbstractCommand  implements ICommand {

	private String requester;

	private RdInter rdInter;

	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}


	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDINTER;
	}
	
	@Override
	public String getRequester() {
		return requester;
	}

	public Command(int objId,RdInter inter)
	{
		this.rdInter = inter;
		this.pid = objId;
	}
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");
		
	}

	public RdInter getRdInter() {	
		return rdInter;
	}

	public void setRdInter(RdInter rdInter) {
		this.rdInter = rdInter;
	}
}
