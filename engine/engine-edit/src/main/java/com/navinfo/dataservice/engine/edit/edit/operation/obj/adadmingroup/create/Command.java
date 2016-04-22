package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private List<Integer> adAdminIds;

	private Integer pid;
	
	private String parentType;

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
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

	public List<Integer> getAdAdminIds() {
		return adAdminIds;
	}

	public void setAdAdminIds(List<Integer> adAdminIds) {
		this.adAdminIds = adAdminIds;
	}
	
	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");

		JSONObject data = json.getJSONObject("data");
		
		parentType = data.getString("parentType");
		
		pid = data.getInt("pid");

		if (data.containsKey("downAdAdmins")) {

			this.adAdminIds = new ArrayList<Integer>();

			JSONArray array = data.getJSONArray("downAdAdmins");

			for (int i = 0; i < array.size(); i++) {
				JSONObject jo = array.getJSONObject(i);

				int adadminId = jo.getInt("adadminId");
				
				if(!adAdminIds.contains(adadminId))
				{
					adAdminIds.add(adadminId);
				}
			}
		}

	}
	
	public static void main(String[] args) {
	}
}
