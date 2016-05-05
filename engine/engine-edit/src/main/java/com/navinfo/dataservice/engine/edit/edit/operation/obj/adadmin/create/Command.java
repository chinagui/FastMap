package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

/**
 * 
* @Title: Command.java 
* @Description: 前台创建行政区划代表点request数据封装类
* @author 张小龙   
* @date 2016年4月18日 下午2:26:38 
* @version V1.0
 */
public class Command extends AbstractCommand implements ICommand {

	private String requester;

	/**
	 * 引导线RDLink的pid
	 */
	private Integer linkPid;

	/**
	 * 经度
	 */
	private double longitude;

	/**
	 * 维度
	 */
	private double latitude;

	public Integer getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(Integer linkPid) {
		this.linkPid = linkPid;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDBRANCH;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public void createGlmList() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setProjectId(json.getInt("projectId"));

		JSONObject data = json.getJSONObject("data");

		this.linkPid = data.getInt("linkPid");

		this.longitude = data.getDouble("longitude");

		this.latitude = data.getDouble("latitude");
	}

}
