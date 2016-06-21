package com.navinfo.dataservice.engine.edit.edit.operation.obj.zonelink.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

/**
 * @author zhaokk
 	ZONE线参数基础类 
 */
public class Command extends AbstractCommand  {

	private String requester;

	//ZONE pid
	private int linkPid;
	public ZoneLink getZoneLink() {
		return zoneLink;
	}

	public void setZoneLink(ZoneLink zoneLink) {
		this.zoneLink = zoneLink;
	}

	public void setUpdateContent(JSONObject updateContent) {
		this.updateContent = updateContent;
	}
	//ZONE 修改内容
	private JSONObject updateContent;
	//ZONE 修改对象
	private ZoneLink zoneLink;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}
	public JSONObject getUpdateContent() {
		return updateContent;
	}
	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	@Override
	public ObjType getObjType() {
		return ObjType.ZONELINK;
	}
	@Override
	public String getRequester() {
		return requester;
	}
	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.updateContent = json.getJSONObject("data");
		this.linkPid = this.updateContent.getInt("pid");
	}

}
