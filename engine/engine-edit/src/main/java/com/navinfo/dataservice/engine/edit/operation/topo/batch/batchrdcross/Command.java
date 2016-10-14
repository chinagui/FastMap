package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chaixin on 2016/10/12 0012.
 */
public class Command extends AbstractCommand {
    private String requester;

    // RdCross的Pid
    private int pid;

    // RdCrossNode的pid数组
    private List<Integer> nodePids;

    private RdCross rdCross;

    public void setRdCross(RdCross rdCross) {
        this.rdCross = rdCross;
    }

    public int getPid() {
        return pid;
    }

    public List<Integer> getNodePids() {
        return nodePids;
    }

    public RdCross getRdCross() {
        return rdCross;
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
        return ObjType.RDCROSS;
    }

    public Command(JSONObject json, String requester) {
        this.setDbId(json.getInt("dbId"));
        this.requester = requester;
        JSONObject data = json.getJSONObject("data");
        if (data.containsKey("pid")) {
            this.pid = data.getInt("pid");
        }
        if (data.containsKey("nodePids")) {
            JSONArray array = data.getJSONArray("nodePids");
            this.nodePids = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                this.nodePids.add(array.getInt(i));
            }
        }
    }
}
