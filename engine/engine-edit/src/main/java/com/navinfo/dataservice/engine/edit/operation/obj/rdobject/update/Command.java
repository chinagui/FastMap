package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
* @ClassName: Command 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:18 
* @Description: TODO
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;
	
	private JSONArray interArray;
	
	private JSONArray roadArray;
	
	private JSONArray linkArray;
	
	private JSONArray nameArray;
	
	private RdObject rdObject;
	
	private double longitude;

	private double latitude;
	
	private int pid;
	
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
		return ObjType.RDOBJECT;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");
		
		if(content.containsKey("inters"))
		{
			this.interArray = content.getJSONArray("inters");
		}
		
		if(content.containsKey("roads"))
		{
			this.roadArray = content.getJSONArray("roads");
		}
		
		if(content.containsKey("links"))
		{
			this.linkArray = content.getJSONArray("links");
		}
		
		if(content.containsKey("names"))
		{
			this.nameArray = content.getJSONArray("names");
		}
		
		if(content.containsKey("geometry"))
		{
			this.latitude = content.getJSONObject("geometry").getDouble("latitude");
			
			this.longitude = content.getJSONObject("geometry").getDouble("longitude");
		}
	}

	public JSONArray getNameArray() {
		return nameArray;
	}

	public void setNameArray(JSONArray nameArray) {
		this.nameArray = nameArray;
	}

	public JSONArray getInterArray() {
		return interArray;
	}

	public void setInterArray(JSONArray interArray) {
		this.interArray = interArray;
	}

	public JSONArray getRoadArray() {
		return roadArray;
	}

	public void setRoadArray(JSONArray roadArray) {
		this.roadArray = roadArray;
	}

	public JSONArray getLinkArray() {
		return linkArray;
	}

	public void setLinkArray(JSONArray linkArray) {
		this.linkArray = linkArray;
	}

	public RdObject getRdObject() {
		return rdObject;
	}

	public void setRdObject(RdObject rdObject) {
		this.rdObject = rdObject;
	}
}
