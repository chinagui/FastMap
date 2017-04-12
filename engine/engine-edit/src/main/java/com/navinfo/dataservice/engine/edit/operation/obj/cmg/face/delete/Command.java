package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete;

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
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.delete
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
     * CMG-FACE对象
     */
    private CmgBuildface cmgface;

    /**
     * 受到影响的CMG-LINK对象
     */
    private List<CmgBuildlink> cmglinks;

    /**
     * 受到影响的CMG-NODE对象
     */
    private List<CmgBuildnode> cmgnodes;

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
        return ObjType.CMGBUILDFACE;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        cmgface.setPid(json.getInt("objId"));
    }

    /**
     * Getter method for property <tt>cmgface</tt>.
     *
     * @return property value of cmgface
     */
    public CmgBuildface getCmgface() {
        return cmgface;
    }

    /**
     * Setter method for property <tt>cmgface</tt>.
     *
     * @param cmgface value to be assigned to property cmgface
     */
    public void setCmgface(CmgBuildface cmgface) {
        this.cmgface = cmgface;
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
}
