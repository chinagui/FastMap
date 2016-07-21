package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * 
* @ClassName: Command 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:18 
* @Description: TODO
 */
public class Command extends AbstractCommand  implements ICommand {

	private String requester;


	private JSONObject content;
	
	private RdTrafficsignal rdTrafficsignal;
	
	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}


	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADADMIN;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public RdTrafficsignal getRdTrafficsignal() {
		return rdTrafficsignal;
	}

	public void setRdTrafficsignal(RdTrafficsignal rdTrafficsignal) {
		this.rdTrafficsignal = rdTrafficsignal;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");

	}

}
