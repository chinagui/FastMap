package com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.face.update
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
     * 修改数据
     */
    private JSONObject content;

    /**
     * 待修改对象
     */
    private CmgBuildface cmgface = new CmgBuildface();

    /**
     * @return 操作类型
     */
    @Override
    public OperType getOperType() {
        return OperType.UPDATE;
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

        content = json.getJSONObject("data");
        cmgface.setPid(content.getInt("pid"));
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
     * Getter method for property <tt>content</tt>.
     *
     * @return property value of content
     */
    public JSONObject getContent() {
        return content;
    }
}
