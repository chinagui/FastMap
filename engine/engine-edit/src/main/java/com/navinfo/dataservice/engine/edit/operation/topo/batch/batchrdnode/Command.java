package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdnode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

    private String requester;

    private JSONArray content;

    public JSONArray getContent() {
    	
        return content;
    }

    public Command(JSONObject json, String requester) {
    	
        this.requester = requester;
        
        setDbId(json.getInt("dbId"));
        
        content = json.getJSONArray("data");       
    }

    @Override
    public OperType getOperType() {
    	
        return OperType.BATCH;
    }

    @Override
    public String getRequester() {
    	
        return this.requester;
    }

    @Override
    public ObjType getObjType() {
    	
        return ObjType.RDNODE;
    }

}