package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.create
 * @Description: 组装创建CMG-FACE所需参数
 * @Author: Crayeres
 * @Date: 2017/4/11
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * CMG-FACE的几何
     */
    private JSONObject geometry;

    /**
     * 线构面时的线PID
     */
    private List<Integer> linkPids = new ArrayList<>();

    /**
     * 线构面时的线对象
     */
    private List<IRow> cmglinks = new ArrayList<>();

    /**
     * @return 操作类型
     */
    @Override
    public OperType getOperType() {
        return OperType.CREATE;
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
        return ObjType.CMGBUILDFACE;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");
        if (data.containsKey("geometry")) {
            this.geometry = data.getJSONObject("geometry");
        } else {
            for (Object linkPid : data.getJSONArray("linkPids")) {
                linkPids.add(Integer.valueOf(linkPid.toString()));
            }
        }
    }

    /**
     * Getter method for property <tt>geometry</tt>.
     *
     * @return property value of geometry
     */
    public JSONObject getGeometry() {
        return geometry;
    }

    /**
     * Getter method for property <tt>linkPids</tt>.
     *
     * @return property value of linkPids
     */
    public List<Integer> getLinkPids() {
        return linkPids;
    }

    /**
     * Getter method for property <tt>cmglinks</tt>.
     *
     * @return property value of cmglinks
     */
    public List<IRow> getCmglinks() {
        return cmglinks;
    }

    /**
     * Setter method for property <tt>cmglinks</tt>.
     *
     * @param cmglinks value to be assigned to property cmglinks
     */
    public void setCmglinks(List<IRow> cmglinks) {
        this.cmglinks = cmglinks;
    }

    /**
     * Setter method for property <tt>linkPids</tt>.
     *
     * @param linkPids value to be assigned to property linkPids
     */
    public void setLinkPids(List<Integer> linkPids) {
        this.linkPids = linkPids;
    }
}
