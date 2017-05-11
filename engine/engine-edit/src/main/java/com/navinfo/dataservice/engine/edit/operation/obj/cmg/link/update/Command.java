package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.update
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
     * 待修改参数
     */
    private JSONObject content;

    /**
     * 待修改CMG-LINK主键
     */
    private CmgBuildlink cmglink = new CmgBuildlink();

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
        return ObjType.CMGBUILDLINK;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        this.content = json.getJSONObject("data");
        this.cmglink.setPid(content.getInt("pid"));
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
     * Getter method for property <tt>content</tt>.
     *
     * @return property value of content
     */
    public JSONObject getContent() {
        return content;
    }
}
