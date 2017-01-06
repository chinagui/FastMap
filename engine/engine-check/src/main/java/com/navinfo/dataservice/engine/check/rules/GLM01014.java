package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

public class GLM01014 extends baseRule {

    public void preCheck(CheckCommand checkCommand) {
    }

    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow obj : checkCommand.getGlmList()) {
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                innerCheck(rdLink);
            }
        }
    }

    public void innerCheck(RdLink rdLink) throws Exception {
        if (rdLink.getsNodePid() == rdLink.geteNodePid()) {
            this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
        }
    }
}