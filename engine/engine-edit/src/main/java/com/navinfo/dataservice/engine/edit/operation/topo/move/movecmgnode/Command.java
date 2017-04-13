package com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.move.movecmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class Command extends AbstractCommand{

    /**
     * 参数
     */
    private String requester;

    /**
     * 待修改对象
     */
    private CmgBuildnode cmgnode;

    /**
     * 经度
     */
    private double longitude;

    /**
     * 纬度
     */
    private double latitude;

    /**
     * 受影响CMG-LINK
     */
    private List<CmgBuildlink> cmglinks;

    /**
     * 受影响CMG-FACE
     */
    private List<CmgBuildface> cmgfaces;

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
        return ObjType.CMGBUILDNODE;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        cmgnode.setPid(json.getInt("objId"));
        longitude = CmgnodeUtil.reviseItude(json.getDouble("longitude"));
        latitude = CmgnodeUtil.reviseItude(json.getDouble("latitude"));
    }

    /**
     * Getter method for property <tt>longitude</tt>.
     *
     * @return property value of longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Getter method for property <tt>latitude</tt>.
     *
     * @return property value of latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter method for property <tt>cmgnode</tt>.
     *
     * @return property value of cmgnode
     */
    public CmgBuildnode getCmgnode() {

        return cmgnode;
    }

    /**
     * Setter method for property <tt>cmgnode</tt>.
     *
     * @param cmgnode value to be assigned to property cmgnode
     */
    public void setCmgnode(CmgBuildnode cmgnode) {
        this.cmgnode = cmgnode;
    }

    /**
     * Getter method for property <tt>cmglinks</tt>.
     *
     * @return property value of cmglinks
     */
    public List<CmgBuildlink> getCmglinks() {
        return cmglinks;
    }

    /**
     * Setter method for property <tt>cmglinks</tt>.
     *
     * @param cmglinks value to be assigned to property cmglinks
     */
    public void setCmglinks(List<CmgBuildlink> cmglinks) {
        this.cmglinks = cmglinks;
    }

    /**
     * Getter method for property <tt>cmgfaces</tt>.
     *
     * @return property value of cmgfaces
     */
    public List<CmgBuildface> getCmgfaces() {
        return cmgfaces;
    }

    /**
     * Setter method for property <tt>cmgfaces</tt>.
     *
     * @param cmgfaces value to be assigned to property cmgfaces
     */
    public void setCmgfaces(List<CmgBuildface> cmgfaces) {
        this.cmgfaces = cmgfaces;
    }
}
