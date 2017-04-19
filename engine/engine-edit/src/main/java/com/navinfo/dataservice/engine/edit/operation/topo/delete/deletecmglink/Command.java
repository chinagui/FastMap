package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecmglink
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
     * 待删除CMG-LINK对象
     */
    private CmgBuildlink cmglink = new CmgBuildlink();

    /**
     * 受影响CMG-NODE对象
     */
    private List<CmgBuildnode> cmgnodes;

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
        return ObjType.CMGBUILDLINK;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        cmglink.setPid(json.getInt("objId"));
    }

    /**
     * Getter method for property <tt>cmglink</tt>.
     *
     * @return property value of cmglink
     */
    public CmgBuildlink getCmglink() {
        return cmglink;
    }

    /**
     * Setter method for property <tt>cmglink</tt>.
     *
     * @param cmglink value to be assigned to property cmglink
     */
    public void setCmglink(CmgBuildlink cmglink) {
        this.cmglink = cmglink;
    }

    /**
     * Getter method for property <tt>cmgnodes</tt>.
     *
     * @return property value of cmgnodes
     */
    public List<CmgBuildnode> getCmgnodes() {
        return cmgnodes;
    }

    /**
     * Setter method for property <tt>cmgnodes</tt>.
     *
     * @param cmgnodes value to be assigned to property cmgnodes
     */
    public void setCmgnodes(List<CmgBuildnode> cmgnodes) {
        this.cmgnodes = cmgnodes;
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
