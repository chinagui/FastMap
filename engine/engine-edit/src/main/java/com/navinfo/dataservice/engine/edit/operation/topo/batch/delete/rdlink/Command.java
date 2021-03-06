package com.navinfo.dataservice.engine.edit.operation.topo.batch.delete.rdlink;


import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ly on 2017/5/2.
 */
public class Command extends AbstractCommand {

    private String requester;

    private List<Integer> linkPids;

    public List<Integer> getLinkPids() {

        return linkPids;
    }

    public Command(JSONObject json, String requester) {

        this.requester = requester;

        setDbId(json.getInt("dbId"));

        linkPids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("objIds")));

    }

    @Override
    public OperType getOperType() {

        return OperType.BATCHDELETE;
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