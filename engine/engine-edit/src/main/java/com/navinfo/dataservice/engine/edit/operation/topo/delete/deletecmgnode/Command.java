package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/12
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 待删除CMG-NODE对象
     */
    private CmgBuildnode cmgnode;

    /**
     * 受影响CMG-LINK对象
     */
    private List<CmgBuildlink> cmglinks;

    /**
     * 受影响CMG-FACE对象
     */
    private List<CmgBuildface> cmgfaces;

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
        return ObjType.CMGBUILDNODE;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        cmgnode.setPid(json.getInt("objId"));
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
