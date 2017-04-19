package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    private CmgBuilding building  = new CmgBuilding();
    
    private int pid;

    public int getPid() {
    	
		return pid;
	}    
    
    public CmgBuilding getBuilding() {
    	
        return building;
    }

    public void setBuilding(CmgBuilding building) {
    	
        this.building = building;
    }

	/**
     * @return 操作类型
     */
    @Override
    public OperType getOperType() {
        return OperType.DELETE;
    }

    /**
     * @return 请求参数
     */
    @Override
    public String getRequester() {
        return this.requester;
    }

    /**
     * @return 操作对象类型
     */
    @Override
    public ObjType getObjType() {
        return ObjType.CMGBUILDING;
    }

    public Command(JSONObject json, String requester) {
    	
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");
    }

}
