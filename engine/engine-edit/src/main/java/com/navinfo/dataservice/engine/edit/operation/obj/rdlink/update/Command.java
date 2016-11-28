package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Command extends AbstractCommand {

    private String requester;

    private int linkPid;

    private List<Integer> linkPids;

    private boolean infect = false;

    private JSONObject updateContent;

    private JSONArray updateContents;

    private List<RdLink> links;

    public int getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    public void setUpdateContent(JSONObject updateContent) {
        this.updateContent = updateContent;
    }

    public JSONObject getUpdateContent() {
        return updateContent;
    }

    public boolean isInfect() {
        return infect;
    }

    public JSONArray getUpdateContents() {
        return updateContents;
    }

    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDLINK;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    public List<Integer> getLinkPids() {
        return linkPids;
    }

    public List<RdLink> getLinks() {
        return links;
    }

    public void setLinks(List<RdLink> links) {
        this.links = links;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        if (json.containsKey("linkPids")) {
            updateContents = json.getJSONArray("data");
            linkPids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("linkPids")));
            links = new ArrayList<>();
        } else {
            updateContent = json.getJSONObject("data");
            linkPid = this.updateContent.getInt("pid");
        }
        // 参数包含infect则认为启用检查
        if (json.containsKey("infect"))
            infect = true;

    }

}
