package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

	private int detailId;
	
	private int branchType;
	
	private String rowId;
	
	private ObjType objType = ObjType.RDBRANCH;
	
	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return this.objType;
	}

	public void setObjType(ObjType objType) {
		this.objType = objType;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public int getBranchType() {
		return branchType;
	}

	public void setBranchType(int branchType) {
		this.branchType = branchType;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		if(json.containsKey("detailId"))
		{
			this.detailId = json.getInt("detailId");
		}
		
		if(json.containsKey("branchType"))
		{
			this.branchType = json.getInt("branchType");
			
			if(this.branchType == 5 || this.branchType == 7)
			{
				if(json.containsKey("rowId"))
				{
					this.rowId = json.getString("rowId");
				}
				else
				{
					throw new Exception("请求中缺少参数：rowId");
				}
			}
		}
		else
		{
			throw new Exception("请求中缺少参数：branchType");
		}
	}

}
