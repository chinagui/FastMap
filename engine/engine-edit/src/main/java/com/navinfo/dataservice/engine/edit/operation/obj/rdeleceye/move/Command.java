package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.move;


import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @author zhangyt
 * @Title: Command.java
 * @Description: TODO
 * @date: 2016年7月29日 下午3:39:08
 * @version: v1.0
 */
public class Command extends AbstractCommand {

    private String requester;

    private RdElectroniceye eleceye;

    private int pid;

    private RdLink link;

    private JSONObject content = new JSONObject();

    public RdElectroniceye getEleceye() {
        return eleceye;
    }

    public void setEleceye(RdElectroniceye eleceye) {
        this.eleceye = eleceye;
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

    public RdLink getLink() {
        return link;
    }

    public void setLink(RdLink link) {
        this.link = link;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");
        this.pid = data.getInt("pid");
        int linkPid = data.getInt("linkPid");
        double latitude = DoubleUtil.keepSpecDecimal(data.getDouble("latitude"));
        double longitude = DoubleUtil.keepSpecDecimal(data.getDouble("longitude"));
        int direct = -1;
        if (data.containsKey("direct"))
            direct = Integer.valueOf(data.get("direct").toString());

        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{longitude, latitude});
        content.put("geometry", geoPoint);
        content.put("linkPid", linkPid);
        if (-1 != direct) {
            content.put("direct", direct);
        }
    }

    @Override
    public OperType getOperType() {
        return OperType.MOVE;
    }

    @Override
    public String getRequester() {
        return this.requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDELECTRONICEYE;
    }

}
