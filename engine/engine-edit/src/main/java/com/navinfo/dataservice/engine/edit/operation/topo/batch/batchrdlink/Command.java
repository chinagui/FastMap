package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlink;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by chaixin on 2016/11/23 0023.
 */
public class Command extends AbstractCommand {

    private String requester;

    //    private List<Integer> linkPids;
    private JSONArray linkPids;

    private JSONArray content;

    public JSONArray getContent() {
        return content;
    }

    //    public List<Integer> getLinkPids() {
//        return linkPids;
//    }

    public JSONArray getLinkPids() {
        return linkPids;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        setDbId(json.getInt("dbId"));
//        linkPids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("linkPids")));
        linkPids = json.getJSONArray("linkPids");
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
        return ObjType.RDLINK;
    }
}
