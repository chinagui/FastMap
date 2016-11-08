package com.navinfo.dataservice.engine.edit.operation.obj.hgwg.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;
import org.apache.hadoop.hdfs.protocol.datatransfer.Op;

/**
 * Created by chaixin on 2016/11/8 0008.
 */
public class Command extends AbstractCommand {

    private String requester;

    private Integer objId;

    private RdHgwgLimit hgwgLimit;

    public Integer getObjId() {
        return objId;
    }

    public RdHgwgLimit getHgwgLimit() {
        return hgwgLimit;
    }

    public void setHgwgLimit(RdHgwgLimit hgwgLimit) {
        this.hgwgLimit = hgwgLimit;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        this.objId = json.getInt("objId");
    }

    @Override
    public OperType getOperType() {
        return OperType.DELETE;
    }

    @Override
    public String getRequester() {
        return this.requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDHGWGLIMIT;
    }
}
