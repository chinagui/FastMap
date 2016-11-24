package com.navinfo.dataservice.engine.edit.operation.batch.rdlink;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author 赵凯凯
 * @version V1.0
 * @Title: Command.java
 * @Description:RdLink批处理操作类
 * @date 2016年8月15日 下午2:26:38
 */
public class Command extends AbstractCommand implements ICommand {

    private String requester;
    private int pid;
    private String ruleId;

    @Override
    public OperType getOperType() {
        return OperType.ONLINEBATCH;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDLINK;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));
        this.setPid(json.getInt("pid"));
        this.setRuleId(json.getString("ruleId"));
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

}
